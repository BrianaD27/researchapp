import React from "react";
import BookmarkBtn from "../common/BookmarkBtn";

const OpportunityCard = ({availability = "Open"}: {availability?: string}) => {
  return (
    <div className="w-full h-25 flex flex-row justify-between items-center p-4  border border-gray-300  ">
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
          <h1 className="font-normal text-2xl ">Research Opportunity Title</h1>
          <div className="flex flex-row gap-2">
            <p className="text-base text-gray-600">Professor Name</p>
            <svg
              width="2"
              height="25"
              viewBox="0 0 2 25"
              xmlns="http://www.w3.org/2000/svg"
            >
              <line
                x1="1"
                y1="0"
                x2="1"
                y2="25"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
            <p className="text-base text-gray-600">Department Name</p>
            <svg
              width="2"
              height="25"
              viewBox="0 0 2 25"
              xmlns="http://www.w3.org/2000/svg"
            >
              <line
                x1="1"
                y1="0"
                x2="1"
                y2="25"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
            <p className="text-base text-gray-600">Date</p>
            <svg
              width="2"
              height="25"
              viewBox="0 0 2 25"
              xmlns="http://www.w3.org/2000/svg"
            >
              <line
                x1="1"
                y1="0"
                x2="1"
                y2="25"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
            <p className={`text-base font-bold ${availability === 'Open' ? 'text-green-500' : 'text-red-500'}`}>
              {availability}
            </p>
          </div>
        </div>
      </div>

      {/* Buttons */}
      <div className="flex flex-row justify-center items-center">
        <button className="px-4 hover:cursor-pointer py-2 bg-vsu-orange text-white font-semibold rounded-md">
          Details
        </button>
        <BookmarkBtn />
      </div>
    </div>
  );
};

export default OpportunityCard;
