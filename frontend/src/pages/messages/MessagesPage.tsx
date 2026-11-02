import { useCallback, useEffect, useRef, useState, type FormEvent, type KeyboardEvent } from "react";
import { useSearchParams } from "react-router-dom";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import StudentNavBar from "../../components/common/StudentNavBar";
import ActiveDot from "../../components/common/ActiveDot";
import apiClient from "../../api/client";
import { currentRole } from "../../api/session";
import {
  MESSAGE_MAX, messageProblem, shortTime,
  type ChatMessage, type Conversation, type Person,
} from "../../api/messages";

// 1:1 messages between anyone in the VSU app. Only the two people in a
// conversation can read it. Messages you receive can be reported to the admins.
export default function MessagesPage() {
  const [params, setParams] = useSearchParams();
  const openId = Number(params.get("c")) || null;
  const [list, setList] = useState<Conversation[]>([]);
  const [listError, setListError] = useState("");
  const [loading, setLoading] = useState(true);
  const [thread, setThread] = useState<{ conversation: Conversation; messages: ChatMessage[] } | null>(null);
  const [threadError, setThreadError] = useState("");
  const [draft, setDraft] = useState("");
  const [sending, setSending] = useState(false);
  const [picking, setPicking] = useState(false);
  const [query, setQuery] = useState("");
  const [people, setPeople] = useState<Person[]>([]);
  const [searching, setSearching] = useState(false);
  const [contactsError, setContactsError] = useState("");
  const [showActive, setShowActive] = useState(true);
  const [reporting, setReporting] = useState<number | null>(null);
  const [reason, setReason] = useState("");
  const [reportNote, setReportNote] = useState("");
  const [alsoBlock, setAlsoBlock] = useState(false);
  const [confirmBlock, setConfirmBlock] = useState(false);
  const bottom = useRef<HTMLDivElement>(null);
  const lastCount = useRef(0);

  const loadList = useCallback(async () => {
    try {
      const { data } = await apiClient.get<Conversation[]>("/messages/conversations");
      setList(data);
      setListError("");
    } catch (caught) {
      setListError(messageProblem(caught, "Could not load your messages."));
    } finally {
      setLoading(false);
    }
  }, []);

  const loadThread = useCallback(async (id: number) => {
    try {
      const { data } = await apiClient.get<{ conversation: Conversation; messages: ChatMessage[] }>(
        `/messages/conversations/${id}`);
      setThread(data);
      setThreadError("");
      setList((current) => current.map((c) => (c.id === id ? { ...data.conversation, unread: 0 } : c)));
    } catch (caught) {
      setThread(null);
      setThreadError(messageProblem(caught, "Could not open this conversation."));
    }
  }, []);

  useEffect(() => {
    apiClient.get<{ showActive: boolean }>("/messages/settings")
      .then(({ data }) => setShowActive(data.showActive)).catch(() => undefined);
    const first = window.setTimeout(() => void loadList(), 0);
    const timer = window.setInterval(() => void loadList(), 30000);
    return () => { window.clearTimeout(first); window.clearInterval(timer); };
  }, [loadList]);

  useEffect(() => {
    if (!openId) return;
    const first = window.setTimeout(() => void loadThread(openId), 0);
    const timer = window.setInterval(() => void loadThread(openId), 8000);
    return () => { window.clearTimeout(first); window.clearInterval(timer); };
  }, [openId, loadThread]);

  // Scroll to the newest message when new ones arrive.
  useEffect(() => {
    const count = thread?.messages.length ?? 0;
    if (count !== lastCount.current) {
      lastCount.current = count;
      bottom.current?.scrollIntoView({ block: "end" });
    }
  }, [thread]);

  function open(id: number) {
    setPicking(false);
    setConfirmBlock(false);
    setReportNote("");
    setDraft("");
    setThread(null);
    lastCount.current = 0;
    setParams({ c: String(id) });
  }

  function openPicker() {
    setPicking(true);
    setQuery("");
    setPeople([]);
    setContactsError("");
  }

  // Search as you type (after a short pause).
  useEffect(() => {
    if (!picking) return;
    const q = query.trim();
    if (q.length < 2) { setPeople([]); setSearching(false); return; }
    setSearching(true);
    const timer = window.setTimeout(async () => {
      try {
        const { data } = await apiClient.get<Person[]>("/messages/people", { params: { q } });
        setPeople(data);
        setContactsError("");
      } catch (caught) {
        setContactsError(messageProblem(caught, "Could not search."));
      } finally {
        setSearching(false);
      }
    }, 300);
    return () => window.clearTimeout(timer);
  }, [query, picking]);

  async function startWith(person: Person) {
    if (person.conversationId) {
      open(person.conversationId);
      return;
    }
    try {
      const { data } = await apiClient.post<Conversation>("/messages/start", { kind: "account", id: person.accountId });
      setList((current) => (current.some((c) => c.id === data.id) ? current : [data, ...current]));
      open(data.id);
    } catch (caught) {
      setContactsError(messageProblem(caught, "Could not start the conversation."));
    }
  }

  async function send(event?: FormEvent<HTMLFormElement>) {
    event?.preventDefault();
    if (!openId || sending) return;
    const body = draft.trim();
    if (!body) return;
    setSending(true);
    setThreadError("");
    try {
      const { data } = await apiClient.post<ChatMessage>(`/messages/conversations/${openId}`, { body });
      setDraft("");
      setThread((current) => (current ? { ...current, messages: [...current.messages, data] } : current));
      void loadList();
    } catch (caught) {
      setThreadError(messageProblem(caught, "Your message was not sent."));
    } finally {
      setSending(false);
    }
  }

  function onKey(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      void send();
    }
  }

  async function sendReport(messageId: number) {
    try {
      await apiClient.post(`/messages/messages/${messageId}/report`, { reason, block: alsoBlock });
      setReporting(null);
      setReason("");
      setReportNote("Thank you for telling us. A VSU admin will review this privately.");
      if (alsoBlock && openId) void loadThread(openId);
    } catch (caught) {
      setReportNote(messageProblem(caught, "The report was not sent."));
    }
  }

  async function setBlocked(accountId: number, block: boolean) {
    try {
      if (block) await apiClient.post(`/messages/blocks/${accountId}`);
      else await apiClient.delete(`/messages/blocks/${accountId}`);
      setConfirmBlock(false);
      setReportNote(block ? "Blocked. They can't message you or find you in search." : "Unblocked.");
      if (openId) void loadThread(openId);
    } catch (caught) {
      setThreadError(messageProblem(caught, block ? "Could not block." : "Could not unblock."));
    }
  }

  async function toggleActive(show: boolean) {
    setShowActive(show);
    try {
      const { data } = await apiClient.put<{ showActive: boolean }>("/messages/settings", { show });
      setShowActive(data.showActive);
    } catch {
      setShowActive(!show);
    }
  }

  const isStudent = currentRole() === "STUDENT";
  const card = "bg-white rounded-xl shadow-lg";

  return (
    <div className="min-h-screen bg-[#1B51A4]">
      {isStudent ? <StudentNavBar /> : <FacultyNavBar />}
      <main className="max-w-6xl mx-auto p-5 flex flex-col gap-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h1 className="text-2xl text-white">Messages</h1>
          <label className="flex items-center gap-2 text-white text-sm cursor-pointer">
            <input type="checkbox" checked={showActive} onChange={(event) => void toggleActive(event.target.checked)} />
            Show when I'm active
          </label>
        </div>

        {listError ? (
          <p role="alert" className={`${card} p-5 text-red-700`}>{listError}</p>
        ) : (
          <div className="grid md:grid-cols-[320px_1fr] gap-4 min-h-[60vh]">
            <aside className={`${card} p-3 flex flex-col gap-2 overflow-y-auto max-h-[75vh]`}>
              <button type="button" onClick={() => void openPicker()}
                className="bg-blue-700 text-white font-semibold rounded-lg p-2 cursor-pointer">
                New message
              </button>
              {loading && <p className="text-gray-600 p-2">Loading...</p>}
              {!loading && list.length === 0 && (
                <p className="text-gray-600 p-2 text-sm">
                  No conversations yet. Click New message to find anyone by name.
                </p>
              )}
              {list.map((c) => (
                <button key={c.id} type="button" onClick={() => open(c.id)}
                  className={`text-left rounded-lg p-3 border cursor-pointer hover:bg-gray-50 ${
                    openId === c.id ? "border-blue-700 bg-blue-50" : "border-gray-200"}`}>
                  <div className="flex items-center justify-between gap-2">
                    <span className="font-semibold truncate flex items-center gap-2">
                      {c.otherName}
                      <ActiveDot active={c.otherActive} label={false} />
                    </span>
                    <span className="text-xs text-gray-500 shrink-0">{shortTime(c.lastMessageAt)}</span>
                  </div>
                  <div className="flex items-center justify-between gap-2">
                    <span className="text-sm text-gray-600 truncate">{c.lastMessagePreview ?? "No messages yet"}</span>
                    {c.unread > 0 && (
                      <span className="shrink-0 rounded-full bg-red-600 text-white text-xs px-2">{c.unread}</span>
                    )}
                  </div>
                </button>
              ))}
            </aside>

            <section className={`${card} flex flex-col max-h-[75vh]`}>
              {picking ? (
                <div className="p-5 flex flex-col gap-3 overflow-y-auto">
                  <div className="flex items-center justify-between">
                    <h2 className="text-xl font-bold">Start a conversation</h2>
                    <button type="button" onClick={() => setPicking(false)} className="text-blue-700 underline cursor-pointer">
                      Cancel
                    </button>
                  </div>
                  <label className="flex flex-col gap-1">
                    <span className="text-sm text-gray-600">Find anyone: students, faculty, or Research Associates.</span>
                    <input autoFocus value={query} maxLength={100} onChange={(event) => setQuery(event.target.value)}
                      placeholder="Type a name or username"
                      className="border border-gray-400 rounded-lg p-2" />
                  </label>
                  {contactsError && <p role="alert" className="text-red-700">{contactsError}</p>}
                  {query.trim().length < 2 && <p className="text-gray-600">Type at least 2 letters.</p>}
                  {searching && <p className="text-gray-600">Searching...</p>}
                  {!searching && query.trim().length >= 2 && people.length === 0 && !contactsError && (
                    <p className="text-gray-600">No one found with that name.</p>
                  )}
                  {people.map((person) => (
                    <div key={person.accountId} className="flex items-center justify-between gap-3 border rounded-lg p-3">
                      <div className="min-w-0">
                        <p className="font-semibold flex items-center gap-2">
                          <span className="truncate">{person.name}</span> <ActiveDot active={person.active} label={false} />
                        </p>
                        {person.detail && <p className="text-sm text-gray-600">{person.detail}</p>}
                      </div>
                      <button type="button" onClick={() => void startWith(person)}
                        className="bg-blue-700 text-white rounded-lg px-3 py-1.5 text-sm font-semibold cursor-pointer shrink-0">
                        {person.conversationId ? "Open" : "Message"}
                      </button>
                    </div>
                  ))}
                </div>
              ) : !openId ? (
                <p className="m-auto text-gray-600 p-5">Pick a conversation, or start a new one.</p>
              ) : (
                <>
                  <header className="border-b p-4 flex flex-wrap items-start justify-between gap-2">
                    <div>
                      <h2 className="text-lg font-bold">{thread?.conversation.otherName ?? "Loading..."}</h2>
                      <div className="flex items-center gap-3 text-sm text-gray-600">
                        {thread?.conversation.otherDetail && <span>{thread.conversation.otherDetail}</span>}
                        <ActiveDot active={Boolean(thread?.conversation.otherActive)} />
                      </div>
                    </div>
                    {thread && (thread.conversation.blockedByMe ? (
                      <button type="button" onClick={() => void setBlocked(thread.conversation.otherAccountId, false)}
                        className="text-sm border rounded-lg px-3 py-1 cursor-pointer">Unblock</button>
                    ) : confirmBlock ? (
                      <span className="flex items-center gap-2 text-sm">
                        Block {thread.conversation.otherName}?
                        <button type="button" onClick={() => void setBlocked(thread.conversation.otherAccountId, true)}
                          className="bg-red-700 text-white rounded px-3 py-1 cursor-pointer">Block</button>
                        <button type="button" onClick={() => setConfirmBlock(false)}
                          className="border rounded px-3 py-1 cursor-pointer">Cancel</button>
                      </span>
                    ) : (
                      <button type="button" onClick={() => setConfirmBlock(true)}
                        className="text-sm text-gray-600 underline cursor-pointer">Block</button>
                    ))}
                  </header>
                  <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-3 bg-gray-50">
                    {thread && thread.messages.length === 0 && (
                      <p className="text-gray-500 text-center">No messages yet. Say hello.</p>
                    )}
                    {thread?.messages.map((m) => (
                      <div key={m.id} className={`max-w-[80%] flex flex-col ${m.mine ? "self-end items-end" : "self-start items-start"}`}>
                        <span className="text-xs text-gray-500 mb-0.5">{m.senderName} - {shortTime(m.createdAt)}</span>
                        <p className={`rounded-2xl px-4 py-2 whitespace-pre-wrap break-words ${
                          m.mine ? "bg-blue-700 text-white" : "bg-white border"}`}>
                          {m.body}
                        </p>
                        {!m.mine && reporting !== m.id && (
                          <button type="button" onClick={() => { setReporting(m.id); setReason(""); setReportNote(""); setAlsoBlock(false); }}
                            className="text-xs text-gray-500 underline mt-0.5 cursor-pointer">
                            Report
                          </button>
                        )}
                        {reporting === m.id && (
                          <div className="mt-1 bg-white border border-red-300 rounded-lg p-3 flex flex-col gap-2 w-80 max-w-full">
                            <span className="text-sm font-semibold">Report this message</span>
                            <span className="text-xs text-gray-600">
                              A VSU admin will review it privately. The other person won't be told who reported it.
                            </span>
                            <textarea rows={2} maxLength={400} value={reason} onChange={(event) => setReason(event.target.value)}
                              placeholder="What happened? (optional)"
                              className="border border-gray-400 rounded p-1 text-sm resize-none" />
                            <label className="flex items-center gap-2 text-sm">
                              <input type="checkbox" checked={alsoBlock} onChange={(event) => setAlsoBlock(event.target.checked)} />
                              Also block this person
                            </label>
                            <span className="text-xs text-gray-600">If you feel unsafe, contact VSU Police right away.</span>
                            <div className="flex gap-2">
                              <button type="button" onClick={() => void sendReport(m.id)}
                                className="bg-red-700 text-white rounded px-3 py-1 text-sm cursor-pointer">Send report</button>
                              <button type="button" onClick={() => setReporting(null)}
                                className="border rounded px-3 py-1 text-sm cursor-pointer">Cancel</button>
                            </div>
                          </div>
                        )}
                      </div>
                    ))}
                    <div ref={bottom} />
                  </div>
                  <form onSubmit={(event) => void send(event)} className="border-t p-3 flex flex-col gap-2">
                    {reportNote && <p role="status" className="text-sm text-green-800 bg-green-50 rounded p-2">{reportNote}</p>}
                    {threadError && <p role="alert" className="text-red-700 text-sm">{threadError}</p>}
                    <div className="flex gap-2 items-end">
                      <textarea rows={2} maxLength={MESSAGE_MAX} value={draft}
                        disabled={sending || !thread || thread.conversation.blockedByMe}
                        onChange={(event) => setDraft(event.target.value)} onKeyDown={onKey}
                        placeholder={thread?.conversation.blockedByMe
                          ? "You blocked this person. Unblock them to send a message."
                          : "Write a message. Enter sends, Shift+Enter adds a line."}
                        className="flex-1 border border-gray-400 rounded-lg p-2 resize-none" />
                      <button type="submit" disabled={sending || !draft.trim()}
                        className="bg-blue-700 text-white font-semibold rounded-lg px-4 py-2 cursor-pointer disabled:opacity-50">
                        {sending ? "Sending..." : "Send"}
                      </button>
                    </div>
                    <span className="text-xs text-gray-500 self-end">{draft.length}/{MESSAGE_MAX}</span>
                  </form>
                </>
              )}
            </section>
          </div>
        )}
      </main>
    </div>
  );
}
