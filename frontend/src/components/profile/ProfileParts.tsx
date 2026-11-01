import type { ReactNode } from "react";
import { hasText, initialsOf, mediaUrl } from "./profileUtils";

// Shared building blocks for the student and faculty profile pages.

export function ProfileHero({ name, photoUrl, lines, actions, active = false }: {
  name: string;
  photoUrl: string | null | undefined;
  lines: (string | null | undefined)[];
  actions?: ReactNode;
  active?: boolean;
}) {
  return (
    <section className="bg-[#1B51A4] text-white">
      <div className="max-w-6xl mx-auto px-6 py-10 flex flex-wrap items-center gap-7">
        <div className="relative shrink-0">
        {photoUrl ? (
          <img
            src={mediaUrl(photoUrl)}
            alt={`Profile of ${name}`}
            className="w-36 h-36 rounded-full object-cover border-4 border-white shrink-0"
          />
        ) : (
          <div
            role="img"
            aria-label={`${name} has no profile photo`}
            className="w-36 h-36 rounded-full bg-[#DCE6F5] border-4 border-white shrink-0 flex items-center justify-center text-5xl font-bold text-[#1B51A4]"
          >
            {initialsOf(name)}
          </div>
        )}
        {active && (
          <span title="Active now" className="absolute bottom-2 right-2 w-8 h-8 rounded-full bg-green-500 border-4 border-white">
            <span className="sr-only">Active now</span>
          </span>
        )}
        </div>
        <div className="flex-[999_1_320px] min-w-0 flex flex-col gap-1.5">
          <h1 className="text-4xl font-bold leading-tight">{name}</h1>
          {lines.filter(hasText).map((line, index) => (
            <p key={index} className={index === 0 ? "text-lg text-[#E6EDF8]" : "text-[15px] text-[#E6EDF8]"}>
              {line}
            </p>
          ))}
        </div>
        {actions && <div className="flex flex-wrap gap-3">{actions}</div>}
      </div>
    </section>
  );
}

export function ProfileCard({ title, note, children }: {
  title: string;
  note?: string;
  children: ReactNode;
}) {
  return (
    <section className="bg-white rounded-xl p-6 flex flex-col gap-3">
      <div className="flex flex-wrap justify-between items-baseline gap-2">
        <h2 className="text-xl font-semibold">{title}</h2>
        {note && <span className="text-sm text-gray-600">{note}</span>}
      </div>
      {children}
    </section>
  );
}

export function Chip({ children, tone = "blue" }: { children: ReactNode; tone?: "blue" | "green" | "gray" }) {
  const tones = {
    blue: "bg-[#EEF3FB] text-[#1B3F7A]",
    green: "bg-[#E6F4EA] text-[#1E6B34]",
    gray: "bg-[#ECEEF2] text-gray-700",
  };
  return (
    <span className={`${tones[tone]} text-sm font-semibold rounded-full px-3 py-1`}>{children}</span>
  );
}

export function ProfileLayout({ main, side }: { main: ReactNode; side: ReactNode }) {
  return (
    <main className="max-w-6xl mx-auto px-6 pt-8 pb-14 flex flex-wrap gap-6 items-start">
      <div className="flex-[999_1_560px] min-w-0 flex flex-col gap-6">{main}</div>
      <aside className="flex-[1_1_280px] min-w-0 flex flex-col gap-6">{side}</aside>
    </main>
  );
}
