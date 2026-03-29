import React from "react";
import NavBar from "../../components/common/NavBar";
import OpportunityCard from "../../components/cards/OpportunityCard";

const StudentHomePage = () => {
  return (
    <div className="h-screen bg-vsu-blue flex flex-col">
      <div className="relative z-50">
        <NavBar />
      </div>

      <div className="flex-1 flex items-center justify-evenly">
        {/* Search Box */}
        <div className="h-160 flex flex-col align-items-center justify-center gap-5">
          {/* Search Bar */}
          <div className="flex flex-row items-center gap-3">
            <input
              className="w-full p-2 border border-gray-300 rounded-md bg-white"
              type="text"
              placeholder="Search for research projects..."
            />
            <button className="px-4 hover:cursor-pointer py-2 bg-vsu-orange text-white rounded-md">
              Search
            </button>
          </div>

          {/* Search Results Box */}
          <div className="h-150  xl:w-190 lg:w-150 md:w-130 w-100 bg-white rounded-lg shadow-lg flex flex-col justify-start overflow-scroll">
            <OpportunityCard availability="Open" />
            <OpportunityCard availability="Closed" />
            <OpportunityCard availability="Open" />
            <OpportunityCard availability="Closed" />
            <OpportunityCard availability="Open" />
            <OpportunityCard availability="Closed" />
            <OpportunityCard availability="Open" />
            <OpportunityCard availability="Closed" />
            <OpportunityCard />
            <OpportunityCard />
            <OpportunityCard />
            <OpportunityCard />
            <OpportunityCard />
            <OpportunityCard />
            <OpportunityCard />
            <OpportunityCard />
            <OpportunityCard />
          </div>
        </div>

        {/* Opportunity Info Box */}
        <div className="hidden md:flex md:flex-col h-160 xl:w-130 lg:w-100 md:w-80 w-30 bg-white scroll-auto rounded-lg shadow-lg items-center p-4 gap-1">
          <img
            className="w-35 h-auto"
            src="../../src/assets/emptyPfp.png"
            alt=""
          />
          <div className="flex flex-col text-center">
            <p>Professor</p>
            <p>Department</p>
          </div>

          <h1 className="font-semibold text-2xl">Research Opportunity Title</h1>

          <div className="flex flex-row gap-6 mb-2 items-center">
            <h1>Start-End Dates</h1>
            <svg
              width="1"
              height="35"
              viewBox="0 0 1 30"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <line x1="0.5" x2="0.5" y2="40" stroke="#B7B7B7" />
            </svg>

            <h1>Time Commitment</h1>
          </div>

          <svg
            width="468"
            height="1"
            viewBox="0 0 468 1"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <line y1="0.5" x2="468" y2="0.5" stroke="#B7B7B7" />
          </svg>

          <div className="px-6 flex flex-col mt-2 gap-2"> 
            <p className="text-start text-xl">Description:</p>
            <div className="overflow-scroll h-50">
              <p className="px-6 text-lg">
              Lorem ipsum dolor, sit amet consectetur adipisicing elit. Quo
              culpa iste numquam, veniam quis voluptas odit ipsum id fuga rem
              aspernatur tempora tenetur obcaecati vero, adipisci hic! Neque,
              atque optio. Lorem ipsum dolor, sit amet consectetur adipisicing elit. Quo
              culpa iste numquam, veniam quis voluptas odit ipsum id fuga rem
              aspernatur tempora tenetur obcaecati vero, adipisci hic! Neque,
              atque optio.
            </p>

            </div>
            
          </div>

          <div className="flex flex-row gap-4 mt-2">
            <button className="text-white font-semibold text-xl bg-vsu-orange border rounded-3xl py-2 px-8">
              Apply
            </button>
            <button className="text-white font-semibold text-xl bg-vsu-blue border rounded-3xl py-2 px-8">
              Email
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StudentHomePage;
