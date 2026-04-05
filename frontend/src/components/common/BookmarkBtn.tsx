import { useState } from "react";

export default function BookmarkBtn() {
  const [saved, setSaved] = useState(false);

  return (
    <button
      onClick={(e) => {
        e.stopPropagation(); // Prevents the click from bubbling up to the card
        setSaved(!saved)
      }}
      className="p-1 rounded-xl transition-colors cursor-pointer"
      aria-label={saved ? "Remove bookmark" : "Bookmark"}
    >
      <svg
        width="38"
        height="40"
        viewBox="0 0 24 28"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className="transition-colors duration-200"
      >
        <path
          d="M5 2C3.895 2 3 2.895 3 4V26L12 21L21 26V4C21 2.895 20.105 2 19 2H5Z"
          fill={saved ? "#F44336" : "none"}
          stroke={saved ? "#000" : "#000"}
          strokeWidth="1.5"
          strokeLinejoin="round"
          className="transition-all duration-200"
        />
      </svg>
    </button>
  );
}