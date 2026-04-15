import React from "react";
import BookmarkBtn from "../common/BookmarkBtn";

const StudentCard = ({
  applied = "Applied",
  isSelected = false,
  graduateYear = "Graduate Year",
  major = "Major Name",
  student = "Student Name",
  classification = "Junior",
  onSelect = () => {},
}: {
  applied?: string;
  isSelected?: boolean;
    graduateYear?: string;
  major?: string;
  student?: string;
  classification?: string;
  onSelect?: () => void;
}) => {
  return (
    <button
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
          <h1 className="font-normal text-2xl ">{student}</h1>
          <div className="flex flex-row gap-2 items-center">
            <p className="text-base text-gray-600">{major}</p>
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
            <p className="text-base text-gray-600">{classification}</p>
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
            <p className="text-base text-gray-600">{graduateYear}</p>
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
              className={`text-base font-bold ${applied === "Applied" ? "text-green-500" : "text-red-500"}`}
            >
              {applied}
            </p>
          </div>
        </div>
      </div>

      {/* Buttons */}
      <div className="flex flex-row justify-center items-center">
        <BookmarkBtn />
      </div>
    </button>
  );
};

export default StudentCard;
