import React from "react";

const StudentInfoCard = ({
  name = "Student Name",
  major = "Computer Science",
  classification = "Junior",
  graduationYear = 2024,
  email = "student@example.com",
}: {
  name?: string;
  major?: string;
  classification?: string;
  graduationYear?: number;
  email?: string;
}) => {
  const skills = [
    "Python",
    "Java",
    "C++",
    "Machine Learning",
    "Data Analysis",
    "Web Development",
  ];
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
              <p className="pr-2 text-lg">{major}</p>
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
              <p className="px-2 text-lg">{classification}</p>
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
              <p className="px-2 text-lg">{graduationYear}</p>
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
              GPA
            </p>
            <p className="text-lg font-bold">3.8 / 4.0</p>
          </div>
          <div className="bg-gray-100 py-2 w-full px-6 rounded-lg flex flex-col items-start justify-center">
            <p className="uppercase font-semibold text-md text-slate-400">
              applied
            </p>
            <p className="text-lg font-bold">April 3, 2023</p>
          </div>
          <div className="bg-gray-100 py-2 w-full px-6 rounded-lg flex flex-col items-start justify-center">
            <p className="uppercase font-semibold text-md text-slate-400">
              availability
            </p>
            <p className="text-lg font-bold">10 hrs / week</p>
          </div>
          <div className="bg-gray-100 py-2 w-full px-6 rounded-lg flex flex-col items-start justify-center">
            <p className="uppercase font-semibold text-md text-slate-400">
              Has Experience
            </p>
            <p className="text-lg font-bold">Yes</p>
          </div>
        </div>

        <div></div>

        {/* Student Skills */}
        <div className="px-6 flex flex-col my-5 gap-1">
          <p className="text-start text-xl uppercase text-slate-400 font-semibold">
            Skills
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
          Accept
        </button>
        <button className="text-white font-semibold text-xl bg-red-500 border rounded-3xl py-2 px-6">
          Reject
        </button>
        <button className="text-white font-semibold text-xl bg-blue-500 border rounded-3xl py-2 px-6">
          Resume
        </button>
      </div>
    </div>
  );
};

export default StudentInfoCard;
