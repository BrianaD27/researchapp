import React from "react";

const OpportunityBookmarkInfo = ({
  name = "Professor Name",
  department = "Computer Science",
  availability = "Open",
  commitment = "10 hrs / week",
  gpa = "3.5+",
  spots = "3/5",
  classification = "Junior+",
  duration = "3/5/2024 - 8/15/2024",
  email = "professor@example.com",
  savedBtn = "Save",
}: {
  name?: string;
  department?: string;
  availability?: string;
  commitment?: string;
  gpa?: string;
  spots?: string;
  classification?: string;
  email?: string;
  duration?: string;
  savedBtn?: string;
}) => {
  const skills = [
    "Python",
    "Java",
    "C++",
    "Machine Learning",
    "Data Analysis",
    "Web Development",
  ];
  const majors = ["Computer Science", "Computer Engineering", "Engineering"];
  return (
    <div className="hidden md:flex md:flex-col h-173 xl:w-130 lg:w-100 md:w-80 w-30 bg-white scroll-auto rounded-lg shadow-lg items-center p-4 gap-1">
      <div className="w-full overflow-scroll">
        {/* Student General Info */}
        <div className="flex flex-row gap-4 items-center justify-start w-full px-3 mb-2">
          <img
            className="w-35 h-auto"
            src="../../src/assets/emptyPfp.png"
            alt=""
          />
          <div>
            <h1 className="font-semibold text-2xl">{name}</h1>
            <div className="flex flex-row flex-wrap items-center">
              <p className="pr-2 text-lg">{department}</p>
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
                className={`px-2 text-lg font-semibold ${availability === "Open" ? "text-green-500" : "text-red-500"}`}
              >
                {availability}
              </p>
            </div>
            <a href={`mailto:${email}`} className="text-blue-500 underline">
              {email}
            </a>
          </div>
        </div>

        <svg
          width="430"
          height="2"
          viewBox="0 0 430 2"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          className="ml-7 text-gray-300 bg-gray-200"
        >
          <line y1="0.5" x2="430" y2="0.5" strokeWidth="2" />
        </svg>

        {/* Specific Info At Glance */}
        <div className="w-full grid grid-cols-2 gap-3 px-5 mt-5">
          <div className="bg-gray-100 py-2 w-full px-6 rounded-lg flex flex-col items-start justify-center">
            <p className="uppercase font-semibold text-md text-slate-400">
              Minimal GPA
            </p>
            <p className="text-lg font-bold">{gpa}</p>
          </div>
          <div className="bg-gray-100 py-2 w-full px-6 rounded-lg flex flex-col items-start justify-center">
            <p className="uppercase font-semibold text-md text-slate-400">
              classification
            </p>
            <p className="text-lg font-bold">{classification}</p>
          </div>
          <div className="bg-gray-100 py-2 w-full px-6 rounded-lg flex flex-col items-start justify-center">
            <p className="uppercase font-semibold text-md text-slate-400">
              time commitment
            </p>
            <p className="text-lg font-bold">{commitment}</p>
          </div>
          <div className="bg-gray-100 py-2 w-full px-6 rounded-lg flex flex-col items-start justify-center">
            <p className="uppercase font-semibold text-md text-slate-400">
              Spots Available
            </p>
            <p className="text-lg font-bold">{spots}</p>
          </div>
        </div>

        <div className="bg-gray-100 mt-3 w-ful mx-4.5 py-2 px-6 rounded-lg flex flex-col items-start justify-center">
          <p className="uppercase font-semibold text-md text-slate-400">
            Duration
          </p>
          <p className="text-lg font-bold">{duration}</p>
        </div>

        <div></div>

        {/* Required Majors */}
        <div className="px-6 flex flex-col my-5 gap-1">
          <p className="text-start text-xl uppercase text-slate-400 font-semibold">
            Target Majors
          </p>
          <div className="flex flex-row gap-2 flex-wrap">
            {majors.map((major) => (
              <p className="px-4 text-normal bg-[#E3F1FC] rounded-full py-1 font-medium border border-[#B7D0E8]">
                {major}
              </p>
            ))}
            <div>
              <p className="px-6 text-lg"></p>
            </div>
          </div>
        </div>

        {/* Student Skills */}
        <div className="px-6 flex flex-col my-5 gap-1">
          <p className="text-start text-xl uppercase text-slate-400 font-semibold">
            Required Skills
          </p>
          <div className="flex flex-row gap-2 flex-wrap">
            {skills.map((skill) => (
              <p className="px-4 text-normal bg-[#E3F1FC] rounded-full py-1 font-medium border border-[#B7D0E8]">
                {skill}
              </p>
            ))}
            <div>
              <p className="px-6 text-lg"></p>
            </div>
          </div>
        </div>

        {/* Student Description */}
        <div className="px-6 flex flex-col mt-2">
          <p className="text-start text-xl uppercase text-slate-400 font-semibold">
            Description
          </p>
          <div className="">
            <p className="px-6 text-lg">
              Lorem ipsum dolor, sit amet consectetur adipisicing elit. Quo
              culpa iste numquam, veniam quis voluptas odit ipsum id fuga rem
              aspernatur tempora tenetur obcaecati vero, adipisci hic! Neque,
              atque optio. Lorem ipsum dolor, sit amet consectetur adipisicing
              elit. Quo culpa iste numquam, veniam quis voluptas odit ipsum id
              fuga rem aspernatur tempora tenetur obcaecati vero, adipisci hic!
              Neque, atque optio.
            </p>
          </div>
        </div>
      </div>

      <div className="flex flex-row gap-4 mt-2">
        <button className="text-white font-semibold text-xl bg-green-500 border rounded-3xl py-2 px-6">
          Apply
        </button>
        <button className="text-white font-semibold hover:cursor-pointer text-xl bg-orange-500 border rounded-3xl py-2 px-6">
          {savedBtn}
        </button>
        <button className="text-white font-semibold hover:cursor-pointer text-xl bg-blue-500 border rounded-3xl py-2 px-6">
          Email
        </button>
      </div>
    </div>
  );
};

export default OpportunityBookmarkInfo;
