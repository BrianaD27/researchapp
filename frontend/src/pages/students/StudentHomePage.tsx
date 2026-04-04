import NavBar from "../../components/common/NavBar";
import OpportunityCard from "../../components/cards/OpportunityCard";
import React from "react";

const StudentHomePage = () => {
  const [isSortedByOpen, setIsSortedByOpen] = React.useState(false);

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
              className="w-[82%] p-2 border border-gray-300 rounded-md bg-white"
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

          <h1 className="font-semibold text-2xl">Research Opportunity Title</h1>

          <div className="flex flex-col text-center">
            <p>Professor</p>
            <p>Department</p>
          </div>

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
                atque optio. Lorem ipsum dolor, sit amet consectetur adipisicing
                elit. Quo culpa iste numquam, veniam quis voluptas odit ipsum id
                fuga rem aspernatur tempora tenetur obcaecati vero, adipisci
                hic! Neque, atque optio.
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
