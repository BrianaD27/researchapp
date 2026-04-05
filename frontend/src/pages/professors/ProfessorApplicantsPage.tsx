import React from "react";
import FacultyOpportunityCard from "../../components/cards/FacultyOpportunityCard";
import StudentInfoCard from "../../components/cards/StudentInfoCard";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import StudentCard from "../../components/cards/StudentCard";

const ProfessorApplicantsPage = () => {
  // const [isSortedByOpen, setIsSortedByOpen] = React.useState(false);
  const [selectedOpportunity, setSelectedOpportunity] = React.useState(Number);
  const [selectedApplicant, setSelectedApplicant] = React.useState(Number);
  const [selectedFilter, setSelectedFilter] = React.useState("All (1)");

  const filters = ["All (1)", "New (0)", "Accepted (4)", "Rejected (10)"];
  const applicants = [
    {
      availability: "Closed",
      student: "James",
      major: "Computer Science",
      graduateYear: "2023",
      applied: "Not Applied",
    },
    {},
    {},
    {},
    {},
    {},
    {},
  ];
  const opportunities = [
    {},
    {
      title: "Research Paper",
      department: "Biology",
      date: "9/15/2023",
      availability: "Closed",
      applicants: "17",
    },
    {},
    {},
  ];
  return (
    <div className="h-screen bg-[#1B51A4] flex flex-col overflow-scroll">
      <div className="relative z-50">
        <FacultyNavBar />
      </div>

      <div className="pt-5 flex-1 flex items-start justify-evenly">
        {/* Search Box */}
        <div className="flex flex-col align-items-center justify-start">
          {/* Search Bar */}
          {/* <div className="flex flex-row items-center gap-3 pb-4">
            <input
              className="w-[73%] p-2 border border-gray-300 rounded-md bg-white"
              type="text"
              placeholder="Search for research projects..."
            />
            <div className="relative">
              <button
                onClick={() => setIsSortedByOpen(!isSortedByOpen)}
                className="px-4 hover:cursor-pointer py-2 bg-vsu-orange font-semibold text-white rounded-md flex flex-row items-center gap-1"
              >
                Sort By
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

              {isSortedByOpen && (
                <div className="flex flex-col absolute bg-white shadow-lg w-full">
                  <button className="hover:bg-gray-200 py-3 border-b border-gray-300">Most Recent</button>
                  <button className="hover:bg-gray-200 py-3 border-b border-gray-300">Least Recent</button>
                  <button className="hover:bg-gray-200 py-3 border-b border-gray-300">Applied</button>
                  <button className="hover:bg-gray-200 py-3 border-b border-gray-300">Not Applied</button>
                  <button className="hover:bg-gray-200 py-3 border-b border-gray-300">Bookmarked</button>
                </div>
              )}
            </div>

            <svg
              width="50"
              height="50"
              viewBox="0 0 73 73"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
              xmlnsXlink="http://www.w3.org/1999/xlink"
            >
              <rect width="73" height="73" fill="url(#pattern0_11_521)" />
              <defs>
                <pattern
                  id="pattern0_11_521"
                  patternContentUnits="objectBoundingBox"
                  width="1"
                  height="1"
                >
                  <use
                    xlinkHref="#image0_11_521"
                    transform="matrix(0.01 0 0 0.01 -0.00046439 0.00264685)"
                  />
                </pattern>
                <image
                  id="image0_11_521"
                  width="100"
                  height="100"
                  preserveAspectRatio="none"
                  xlinkHref="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGQAAABkCAYAAABw4pVUAAAACXBIWXMAAAsTAAALEwEAmpwYAAAApElEQVR4nO3awQnEMBAEQQfiDC50BzmXg9GjMVWwEfRLjK4LAAAAAAAAAHhl273t53bi7hNBnnHKI0iLIDGCxAgSI0iMIDGCfDGIh+FCD0MAAAAAAOA4e8hae4jF8CgTbowgMYLECBIjSIwgMYLE+Ci31vkoBwAAAAAAQfaQtfYQi+FRJtwYQWIEiREkRpAYQWIEifFRbq3zUQ4AAAAAAAAArnf+vVSI+NX0C4YAAAAASUVORK5CYII="
                />
              </defs>
            </svg>
          </div> */}

          <p className="text-white/70 text-xl pb-2 uppercase">
            My Opportunities
          </p>

          {/* Opportunity Results Box */}
          <div className="h-55  xl:w-190 lg:w-150 md:w-130 w-100 gap-2 mb-3 shadow-lg flex flex-col justify-start overflow-scroll">
            {opportunities.map((opportunity, index) => (
              <FacultyOpportunityCard
                key={index}
                title={opportunity.title}
                department={opportunity.department}
                date={opportunity.date}
                availability={opportunity.availability}
                applicants={opportunity.applicants}
                isSelected={selectedOpportunity === index}
                onSelect={() => setSelectedOpportunity(index)}
              />
            ))}
          </div>

          <p className="text-white pt-2 text-xl font-semibold">
            Applicants - Opportunity Title 1
          </p>

          {/* Filter Buttons */}
          <div className="flex flex-row items-start gap-3 py-2">
            {filters.map((filter) => (
              <button
                onClick={() => setSelectedFilter(filter)}
                className={`py-1 px-5 border border-white rounded-3xl font-semibold text-white ${selectedFilter === filter ? "" : "hover:bg-white/40 "} cursor-pointer ${selectedFilter === filter ? "bg-vsu-orange" : "bg-white/20"}`}
              >
                {filter}
              </button>
            ))}
          </div>

          {/* Search Results Box */}
          <div className="h-85  xl:w-190 lg:w-150 md:w-130 w-100 gap-2 shadow-lg flex flex-col justify-start overflow-scroll">
            {applicants.map((applicant, index) => (
              <StudentCard
                key={index}
                graduateYear={applicant.graduateYear}
                student={applicant.student}
                major={applicant.major}
                applied={applicant.applied}
                isSelected={selectedApplicant === index}
                onSelect={() => setSelectedApplicant(index)}
              />
            ))}
          </div>
        </div>

        {/* Student Info Box */}
        <div>
          <StudentInfoCard />
        </div>
      </div>
    </div>
  );
};

export default ProfessorApplicantsPage;
