// Small green "active now" dot, like Teams.
export default function ActiveDot({ active, label = true }: { active: boolean; label?: boolean }) {
  if (!active) return null;
  return (
    <span className="inline-flex items-center gap-1 text-xs text-green-700" title="Active now">
      <span aria-hidden="true" className="inline-block w-2.5 h-2.5 rounded-full bg-green-500 ring-2 ring-white" />
      {label ? "Active now" : <span className="sr-only">Active now</span>}
    </span>
  );
}
