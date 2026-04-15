import { useState, useRef } from "react";

interface SkillInputProps {
  value: string[];
  onChange: (skills: string[]) => void;
}

export default function SkillInput({ value, onChange }: SkillInputProps) {
  const [input, setInput] = useState("");
  const inputRef = useRef<HTMLInputElement>(null);

  const addSkill = () => {
    if (!input.trim() || value.includes(input.trim())) return;
    onChange([...value, input.trim()]);
    setInput("");
  };

  const removeSkill = (skill: string) => {
    onChange(value.filter((s) => s !== skill));
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      e.preventDefault();
      addSkill();
    }
    if (e.key === "Backspace" && !input && value.length) {
      removeSkill(value[value.length - 1]);
    }
  };

  return (
    <div
      className="flex flex-wrap gap-1.5 items-start h-[70px] px-2.5 py-2
                 bg-gray-50 border border-slate-400 rounded-lg cursor-text w-full overflow-scroll"
      onClick={() => inputRef.current?.focus()}
    >
      {value.map((skill) => (
        <span
          key={skill}
          className="flex items-center gap-1 bg-blue-50 text-blue-800
                     border border-blue-200 rounded-full text-xs px-3 py-1 shrink-0"
        >
          {skill}
          <button
            onClick={() => removeSkill(skill)}
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
        placeholder="+ add skill..."
        className="w-fit min-w-20 bg-transparent outline-none
                   text-sm text-gray-700 placeholder:text-gray-400"
      />
    </div>
  );
}