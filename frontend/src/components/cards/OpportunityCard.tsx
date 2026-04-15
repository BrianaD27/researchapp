import React from "react";
import BookmarkBtn from "../common/BookmarkBtn";

const OpportunityCard = ({
  availability = "Open",
  isSelected = false,
  title = "Research Opportunity Title",
  department = "Department Name",
  professor = "Professor Name",
  date = "Date Added",
  onSelect = () => {},
}: {
  availability?: string;
  isSelected?: boolean;
  title?: string;
  department?: string;
  professor?: string;
  date?: string;
  onSelect?: () => void;
}) => {
  return (
    <div
      role="button"
      className={`cursor-pointer ${isSelected ? "" : "hover:bg-white/90"} hover:border-4 text-start w-full h-25 flex flex-row justify-between items-center p-4  border rounded-2xl bg-white border-gray-300 ${isSelected ? "border-vsu-orange border-4" : ""} `}
      onClick={onSelect}
    >
      {/* Opportunity Info */}
      <div className="flex flex-row justify-center items-center gap-3">
        {/* Profile Picture */}
        <img
          className="w-17 h-auto"
          src="../../src/assets/emptyPfp.png"
          alt=""
        />

        {/* Opportunity Description */}
        <div className="flex flex-col gap-2">
          <h1 className="font-normal text-2xl ">{title}</h1>
          <div className="flex flex-row gap-2 items-center">
            <p className="text-base text-gray-600">{professor}</p>
            <svg
              width="5"
              height="5"
              viewBox="0 0 5 5"
              xmlns="http://www.w3.org/2000/svg"
            >
              <circle
                cx="2.5"
                cy="2.5"
                r="2.5"
                fill="currentColor"
                opacity="0.4"
              />
            </svg>
            <p className="text-base text-gray-600">{department}</p>
            <svg
              width="5"
              height="5"
              viewBox="0 0 5 5"
              xmlns="http://www.w3.org/2000/svg"
            >
              <circle
                cx="2.5"
                cy="2.5"
                r="2.5"
                fill="currentColor"
                opacity="0.4"
              />
            </svg>
            <p className="text-base text-gray-600">{date}</p>
            <svg
              width="5"
              height="5"
              viewBox="0 0 5 5"
              xmlns="http://www.w3.org/2000/svg"
            >
              <circle
                cx="2.5"
                cy="2.5"
                r="2.5"
                fill="currentColor"
                opacity="0.4"
              />
            </svg>
            <p
              className={`text-base font-bold ${availability === "Open" ? "text-green-500" : "text-red-500"}`}
            >
              {availability}
            </p>
          </div>
        </div>
      </div>

      {/* Buttons */}
      <div className="flex flex-row justify-center items-center">
        <BookmarkBtn />
      </div>
    </div>
  );
};

export default OpportunityCard;
