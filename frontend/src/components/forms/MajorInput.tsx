import { useState, useRef } from "react";

interface MajorInputProps {
  value: string[];
  onChange: (majors: string[]) => void;
}

export default function MajorInput({ value, onChange }: MajorInputProps) {
  const [input, setInput] = useState("");
  const inputRef = useRef<HTMLInputElement>(null);

  const addMajor = () => {
    if (!input.trim() || value.includes(input.trim())) return;
    onChange([...value, input.trim()]);
    setInput("");
  };

  const removeMajor = (major: string) => {
    onChange(value.filter((s) => s !== major));
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      e.preventDefault();
      addMajor();
    }
    if (e.key === "Backspace" && !input && value.length) {
      removeMajor(value[value.length - 1]);
    }
  };

  return (
    <div
      className="flex flex-wrap gap-1.5 items-start h-17.5 px-2.5 py-2
                 bg-gray-50 border border-slate-400 rounded-lg cursor-text w-full overflow-scroll"
      onClick={() => inputRef.current?.focus()}
    >
      {value.map((major) => (
        <span
          key={major}
          className="flex items-center gap-1 bg-blue-50 text-blue-800
                     border border-blue-200 rounded-full text-xs px-3 py-1 shrink-0"
        >
          {major}
          <button
            onClick={() => removeMajor(major)}
            className="text-blue-400 hover:text-blue-700"
          >
            ×
          </button>
        </span>
      ))}

      <input
        ref={inputRef}
        value={input}
        onChange={(e) => setInput(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder="+ add major..."
        className="w-fit min-w-20 bg-transparent outline-none
                   text-sm text-gray-700 placeholder:text-gray-400"
      />
    </div>
  );
}