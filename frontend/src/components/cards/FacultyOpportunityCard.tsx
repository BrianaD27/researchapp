import React from "react";

const FacultyOpportunityCard = ({
  availability = "Open",
  applicants = "7",
  title = "Research Opportunity Title",
  department = "Department Name",
  date = "Date Added",
  isSelected = false,
  onSelect = () => {},
}: {
  availability?: string;
  applicants?: string;
  title?: string;
  department?: string;
  date?: string;
  isSelected?: boolean;
  onSelect?: () => void;
}) => {

  return (
    <button
      className={`w-full hover:cursor-pointer h-20 ${isSelected ? "": "hover:bg-white/30"} flex flex-row justify-between items-center p-4 border rounded-2xl border-gray-300 ${isSelected ? "bg-white" : "bg-white/15"} `}
      onClick={onSelect}
    >
      {/* Opportunity Info */}
      <div className="flex flex-row justify-center items-center gap-3">
        {/* Opportunity Description */}
        <div className="flex flex-col gap- text-start">
          <h1
            className={`font-normal text-2xl ${isSelected ? "text-black" : "text-white"}`}
          >
            {title}
          </h1>

          <div className="flex flex-row gap-2 items-center">
            <p
              className={`text-base  ${isSelected ? "text-gray-600" : "text-white/60"}`}
            >
              {department}
            </p>
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
              className={`text-base  ${isSelected ? "text-gray-600" : "text-white/60"}`}
            >
              {date}
            </p>
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
        <p className="py-1 px-5 bg-vsu-blue border border-white rounded-3xl font-semibold text-white cursor-pointer">
          {applicants} Applicants
        </p>
      </div>
    </button>
  );
};

export default FacultyOpportunityCard;
