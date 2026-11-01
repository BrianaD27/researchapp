import { useCallback, useEffect, useRef, useState, type FormEvent, type KeyboardEvent } from "react";
import { useSearchParams } from "react-router-dom";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import StudentNavBar from "../../components/common/StudentNavBar";
import ActiveDot from "../../components/common/ActiveDot";
import apiClient from "../../api/client";
import {
  MESSAGE_MAX, messageProblem, shortTime,
  type ChatMessage, type Contact, type Conversation,
} from "../../api/messages";

// 1:1 messages between students and faculty. Only the two sides can read a thread.
export default function MessagesPage() {
  const [params, setParams] = useSearchParams();
  const openId = Number(params.get("c")) || null;
  const [role, setRole] = useState("");
  const [list, setList] = useState<Conversation[]>([]);
  const [listError, setListError] = useState("");
  const [loading, setLoading] = useState(true);
  const [thread, setThread] = useState<{ conversation: Conversation; messages: ChatMessage[] } | null>(null);
  const [threadError, setThreadError] = useState("");
  const [draft, setDraft] = useState("");
  const [sending, setSending] = useState(false);
  const [picking, setPicking] = useState(false);
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [contactsError, setContactsError] = useState("");
  const [showActive, setShowActive] = useState(true);
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
    apiClient.get<{ role: string }>("/associates/session").then(({ data }) => setRole(data.role)).catch(() => undefined);
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
    setDraft("");
    setThread(null);
    lastCount.current = 0;
    setParams({ c: String(id) });
  }

  async function openPicker() {
    setPicking(true);
    setContactsError("");
    try {
      const { data } = await apiClient.get<Contact[]>("/messages/contacts");
      setContacts(data);
    } catch (caught) {
      setContactsError(messageProblem(caught, "Could not load contacts."));
    }
  }

  async function startWith(contact: Contact) {
    if (contact.conversationId) {
      open(contact.conversationId);
      return;
    }
    try {
      const { data } = await apiClient.post<Conversation>("/messages/start", { targetId: contact.id });
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

  async function toggleActive(show: boolean) {
    setShowActive(show);
    try {
      const { data } = await apiClient.put<{ showActive: boolean }>("/messages/settings", { show });
      setShowActive(data.showActive);
    } catch {
      setShowActive(!show);
    }
  }

  const isStudent = role === "STUDENT";
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
                  No conversations yet. {isStudent
                    ? "After you apply to a project, you can message its professor."
                    : "You can message students who applied to your projects."}
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
                  <p className="text-sm text-gray-600">
                    {isStudent
                      ? "You can message professors of projects you applied to."
                      : "Students who applied to your projects."}
                  </p>
                  {contactsError && <p role="alert" className="text-red-700">{contactsError}</p>}
                  {!contactsError && contacts.length === 0 && <p className="text-gray-600">No one to message yet.</p>}
                  {contacts.map((contact) => (
                    <div key={contact.id} className="flex items-center justify-between gap-3 border rounded-lg p-3">
                      <div>
                        <p className="font-semibold flex items-center gap-2">
                          {contact.name} <ActiveDot active={contact.active} label={false} />
                        </p>
                        {contact.detail && <p className="text-sm text-gray-600">{contact.detail}</p>}
                      </div>
                      <button type="button" onClick={() => void startWith(contact)}
                        className="bg-blue-700 text-white rounded-lg px-3 py-1.5 text-sm font-semibold cursor-pointer">
                        {contact.conversationId ? "Open" : "Message"}
                      </button>
                    </div>
                  ))}
                </div>
              ) : !openId ? (
                <p className="m-auto text-gray-600 p-5">Pick a conversation, or start a new one.</p>
              ) : (
                <>
                  <header className="border-b p-4">
                    <h2 className="text-lg font-bold">{thread?.conversation.otherName ?? "Loading..."}</h2>
                    <div className="flex items-center gap-3 text-sm text-gray-600">
                      {thread?.conversation.otherDetail && <span>{thread.conversation.otherDetail}</span>}
                      <ActiveDot active={Boolean(thread?.conversation.otherActive)} />
                    </div>
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
                      </div>
                    ))}
                    <div ref={bottom} />
                  </div>
                  <form onSubmit={(event) => void send(event)} className="border-t p-3 flex flex-col gap-2">
                    {threadError && <p role="alert" className="text-red-700 text-sm">{threadError}</p>}
                    <div className="flex gap-2 items-end">
                      <textarea rows={2} maxLength={MESSAGE_MAX} value={draft} disabled={sending || !thread}
                        onChange={(event) => setDraft(event.target.value)} onKeyDown={onKey}
                        placeholder="Write a message. Enter sends, Shift+Enter adds a line."
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
