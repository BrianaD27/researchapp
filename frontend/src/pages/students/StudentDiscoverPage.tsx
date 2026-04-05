import React from "react";
import SkillInput from "../../components/forms/SkillInput";
import StudentNavBar from "../../components/common/StudentNavBar";
import OpportunityCard from "../../components/cards/OpportunityCard";
import OpportunityDetailsCard from "../../components/cards/OpportunityDetailsCard";

const StudentDiscoverPage = () => {
  // const [isSortedByOpen, setIsSortedByOpen] = React.useState(false);
  const [selectedOpportunity, setSelectedOpportunity] = React.useState(Number);
  const [skills, setSkills] = React.useState<string[]>([
    "Python",
    "Machine Learning",
  ]);

  const opportunities = [
    {
      availability: "Closed",
      isSelected: false,
      title: "Biology Research Assistant",
      department: "Biology",
      professor: "Dr. Smith",
      date: "9/23/2023",
    },
    {},
    {},
    {},
    {},
    {},
    {},
  ];

  return (
    <div className="h-screen bg-[#1B51A4] flex flex-col overflow-scroll">
      <div className="relative z-50">
        <StudentNavBar />
      </div>

      <div className="pt-5 flex-1 flex items-start justify-evenly">
        {/* Search Box */}
        <div className="flex flex-col align-items-center justify-start">
          <p className="text-white/70 text-xl pb-2 uppercase">Search</p>
          {/* Search Bar */}
          <div className="flex flex-row items-center gap-3 pb-4">
            <input
              className="w-full p-2 border border-gray-300 rounded-md bg-white"
              type="text"
              placeholder="Search for students..."
            />
            <button className="px-4 hover:cursor-pointer py-2 bg-vsu-orange font-semibold text-white rounded-md flex flex-row items-center gap-1">
              Search
              <svg
                width="30"
                height="27"
                viewBox="0 0 50 47"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
                xmlnsXlink="http://www.w3.org/1999/xlink"
              >
                <rect
                  y="47"
                  width="47"
                  height="50"
                  transform="rotate(-90 0 47)"
                  fill="url(#pattern0_5_82)"
                />
                <defs>
                  <pattern
                    id="pattern0_5_82"
                    patternContentUnits="objectBoundingBox"
                    width="1"
                    height="1"
                  >
                    <use
                      xlinkHref="#image0_5_82"
                      transform="matrix(0.01 0 0 0.0094 0 0.03)"
                    />
                  </pattern>
                  <image
                    id="image0_5_82"
                    width="100"
                    height="100"
                    preserveAspectRatio="none"
                    xlinkHref="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGQAAABkCAYAAABw4pVUAAAACXBIWXMAAAsTAAALEwEAmpwYAAAEmElEQVR4nO2dTY9URRSGSzECbojyFTSgK3UpcSsGE8AoAmrUnwDRmRFienPvL3ChRL4WQHThDyAhbkSNJATjDgXRjc4Ajk5MhBFc6UIec0J10jAM81lV59Q9TzKb7k7fW/1O1amPc94bguM4juM4juM4juM4zpwA1gAvAu8CR4CvgAvAKDAJ/Bv/JuNr8t6X8bMjwDZgdel2mAV4CNgFHIg/7k0WjnzHeeAjYCewvHQ7VQPcDzwHHAVukB65xqfADmBJ6farAVgGvAOMUQ4Z5t4GloaOD0s9YAI9TADvyb2FLhGHiUvoZRx4I9QO8ATwGXY4CWwINQK8Gqel1rgBvBVqQQJlnL5a56j5oA+sBL6lHs4CjwSLAI/GRV1t/ASsD5YAngZ+pV6uAE8FCwCPAZepn9+Ax4OBmCFduiv8qDamxNlUTQF8tnyjcvYFHKa7HAiakG0GdDIBfA78kPg6srX/elC0HXIdfbwPPDhwn9uBvxNe7y8VQV7p3lRvmnsdTnzdk/kVmLo/pY3mHve7NsP1d+RV4fbzjMsWekYfYF2GexgrcjwcD5fMiCHEhIcc7AsFjl0njImxMXFQv3MVn29tEs/A1ceMPsAzwNXM97UnZMwOKZmQMJ+ecY38/ALcl0OQF9BBo7RnDPJ8DkE+oTyNATGE46nFWK5gVd4YEaN/Hp9uChzTO0vSUxwzpmN7SkFKJis0hnrGIB+kFKTUGXljVAzhu5QlAYuRhd6FYWqQ/4BVKQSR+ozcNIZ7xiBbUggixTI5aSoRQxhKIYhUI+WiqUgM4WAKQaRELAc94zHjbpxKIUjqc+kae0af8ykEuaRAjI3GekafsRSCpPyv/HqmnVHsiiH8mUIQKTtOxXCFw9Qg/1gTZMQFmSOJf5DTPmTNEQ/q+oJ6jmlvO4v7sDh8JZn2+sJQ2cIw59ZJW1lPOVjD5mJbkShDtWy/t5WIkmT7fbUfUM37gGrlogtS+Ai3NdxTziURIzZazL9K0RoVJWmSgzixlaRncPh6ufZEudZQT7mevFYE+Lh0K7EjyrGkYsSGbkYHrQFRNuUqRxCPQg30FMeUn7OUI8RGimGkFlqlPWV3FjEGStp+Rw89ZSVt49ntNqJ7J8ZEGc50L3vzqDC1LFqbs2hPQVm0lLIty6fE7Q18CX20M7jc2a0HmQ3RStWKtcZI4uueyK/A1EZuiMYrGs1nlt5h3pwyqE+q8WIEXiu0NT8Tf0R7poukRdq+K2iiEm/e+bI/KLX4E7u7rnFm0JdLFcAK4Hu6w0W1JpgdtIkdN2PULybD0Wy4Vq4ATwZLxFWxPPOpRq/e9cEiMr5WFujPAA8HywAPxEWaxnXKbLkZp/U6Z1ML8Emx+kCXN0ONiK+t0r2v6ThhNl7MBeAVRa50d2O0+K5tbmJK0b5oGKlpbbG32HmGoi2XPfFgpxSSkLC7qqC9GADPxtnM1UxJbPLo1S3ZskOswq0kCjHP3y9+UzGDfKHId5wDPpT0zk4PSwsFWAVslcIX4BDwRdzEHI05V/3Hd1+Lr8l7p+Jnh2IvSFMS4DiO4ziO4ziO4zhOqJf/AZA9QlamcigfAAAAAElFTkSuQmCC"
                  />
                </defs>
              </svg>
            </button>
          </div>

          {/* Specific filters for sorting search results (e.g. by date, by major, etc.) */}
          <div className="grid grid-cols-2 gap-2">
            <select
              title="major"
              className="border border-gray-300 rounded-lg px-4 py-2 bg-white text-gray-700 cursor-pointer focus:outline-none focus:ring-2 focus:ring-vsu-orange w-full"
            >
              <option value="">All Majors</option>
              <option value="computer science">Computer Science</option>
              <option value="math">Mathematics</option>
              <option value="business">Business</option>
              <option value="biology">Biology</option>
              <option value="history">History</option>
              <option value="english">English</option>
              <option value="art">Art</option>
              <option value="psychology">Psychology</option>
            </select>
            <select
              title="classification"
              className="border border-gray-300 rounded-lg px-4 py-2 bg-white text-gray-700 cursor-pointer focus:outline-none focus:ring-2 focus:ring-vsu-orange w-full"
            >
              <option value="">All Classifications</option>
              <option value="freshman">Freshman</option>
              <option value="sophomore">Sophomore</option>
              <option value="junior">Junior</option>
              <option value="senior">Senior</option>
            </select>
            <select
              title="gpa"
              className="border border-gray-300 rounded-lg px-4 py-2 bg-white text-gray-700 cursor-pointer focus:outline-none focus:ring-2 focus:ring-vsu-orange w-full"
            >
              <option value="">Any GPA</option>
              <option value="1.5-2.4">1.5-2.4</option>
              <option value="2.5-3.4">2.5-3.4</option>
              <option value="3.5-4.0">3.5-4.0</option>
              <option value="4.0+">4.0+</option>
            </select>
            <select
              title="availability"
              className="border border-gray-300 rounded-lg px-4 py-2 bg-white text-gray-700 cursor-pointer focus:outline-none focus:ring-2 focus:ring-vsu-orange w-full"
            >
              <option value="">Any Availability</option>
              <option value="5">5+ hrs/week</option>
              <option value="10">10+ hrs/week</option>
              <option value="15">15+ hrs/week</option>
            </select>
          </div>

          <div className="flex flex-col align-items-center justify-start w-full max-w-190 pt-3">
            <SkillInput value={skills} onChange={setSkills} />
          </div>

          <div className="flex mt-5 flex-row justify-between items-center">
            <p className="text-white pt-2 text-lg">Matching Students</p>
            <p className="text-white pt-2 text-lg ">24 Results</p>
          </div>

          {/* Search Results Box */}
          <div className="h-93 xl:w-190 lg:w-150 md:w-130 w-100 gap-2 shadow-lg flex flex-col justify-start overflow-scroll">
            {opportunities.map((opportunity, index) => (
              <OpportunityCard
                key={index}
                title={opportunity.title}
                department={opportunity.department}
                professor={opportunity.professor}
                date={opportunity.date}
                availability={opportunity.availability}
                isSelected={selectedOpportunity === index}
                onSelect={() => setSelectedOpportunity(index)}
              />
            ))}
          </div>
        </div>

        {/* Student Info Box */}
        <div>
          <OpportunityDetailsCard />
        </div>
      </div>
    </div>
  );
};

export default StudentDiscoverPage;
