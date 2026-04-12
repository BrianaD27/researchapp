import React, { useState } from "react";
import { useLocation, Link } from "react-router-dom";

const StudentNavBar = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isOpportunitiesDropdownOpen, setIsOpportunitiesDropdownOpen] =
    useState(false);

  const location = useLocation();
  const currentPath = location.pathname;

  const navLinkClass = (path: string) =>
    `nav-link ${currentPath === path ? "text-vsu-orange font-bold border-b-2 border-vsu-orange" : ""}`;

  // Active if on /my-opportunities OR any of its children
  const opportunitiesPaths = ["/my-opportunities", "/saved-opportunities", "/applied-opportunities", "/completed-opportunities"];
  const isOpportunitiesActive = opportunitiesPaths.includes(currentPath);

  return (
    <div className=" flex items-center justify-between p-3 px-7 bg-white text-black drop-shadow-lg">
      <div className="flex items-center space-x-3">
        <img
          src="/src/assets/vsuLogo.png"
          alt="VSU Logo"
          className="lg:w-24 h-auto w-16"
        />

        <div className="w-0.75 h-14 bg-black"></div>

        <div>
          <h4 className="lg:text-lg text-sm font-bold">Trojan Undergrad </h4>
          <h4 className="lg:text-lg text-sm font-bold">
            Research Network
          </h4>
        </div>
      </div>

      {/* PC Menu */}
      <ul className="space-x-4 hidden md:flex">
        <li className="nav-item items-center flex">
          <div className="Dropdown text-sm lg:text-base flex items-center cursor-pointer">
            <Link className={navLinkClass("/discover-opportunities")} to="/discover-opportunities">
              Discover Opportunities
            </Link>
          </div>
        </li>

        <li className="nav-item items-center flex">
          <div className="Dropdown group">
            <div className="Dropdown flex items-center cursor-pointer text-sm lg:text-base ">
              <Link
                to="/saved-opportunities"
                className={`nav-link ${isOpportunitiesActive ? "text-vsu-orange font-bold border-b-2 border-vsu-orange" : ""}`}
              >
                My Opportunities
              </Link>
              <img
                src="/src/assets/orangeDd.png"
                alt=""
                className="w-10 h-auto"
              />
            </div>

            <div className="dropdown-content hidden group-hover:flex flex-col absolute bg-white border border-gray-300">
              <Link
                to="/saved-opportunities"
                className={`hover:bg-gray-100 pr-4 pl-4 pt-2 pb-2 ${currentPath === "/saved-opportunities" ? "text-vsu-orange font-bold" : ""}`}
              >
                Saved
              </Link>
              <Link
                to="/applied-opportunities"
                className={`hover:bg-gray-100 pr-4 pl-4 pt-2 pb-2 ${currentPath === "/applied-opportunities" ? "text-vsu-orange font-bold" : ""}`}
              >
                Applied
              </Link>
              <Link
                to="/completed-opportunities"
                className={`hover:bg-gray-100 pr-4 pl-4 pt-2 pb-2 ${currentPath === "/completed-opportunities" ? "text-vsu-orange font-bold" : ""}`}
              >
                Completed
              </Link>
            </div>
          </div>
        </li>

        <li className="nav-item items-center flex text-sm lg:text-base ">
          <div className="Dropdown group">
            <div className="Dropdown flex items-center cursor-pointer">
              <Link className={navLinkClass("/profile")} to="/profile">
                Profile
              </Link>
            </div>
          </div>
        </li>
      </ul>

      {/* Mobile Menu */}
      <div className="md:hidden block">
        <button
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          className="flex flex-col items-center justify-center space-y-1 hover:cursor-pointer"
        >
          <span className="block w-6 h-0.75 bg-black"></span>
          <span className="block w-6 h-0.75 bg-black"></span>
          <span className="block w-6 h-0.75 bg-black"></span>
        </button>
      </div>

      {isMobileMenuOpen && (
        <div className="absolute top-full right-0 mt-1 w-48 bg-white border border-gray-300 rounded shadow-lg">
          <div className="flex flex-col items-center">
            <div className="w-full flex flex-col items-center hover:cursor-pointer hover:bg-gray-100">
              <Link
                to="/discover-opportunities"
                className={`px-4 py-2 w-full text-center ${currentPath === "/discover-opportunities" ? "text-vsu-orange font-bold" : ""}`}
              >
                Discover
              </Link>
            </div>

            <div className="w-full flex flex-col items-center hover:cursor-pointer hover:bg-gray-100">
              <button
                className={`Dropdown flex items-center cursor-pointer ${isOpportunitiesActive ? "text-vsu-orange font-bold" : ""}`}
                onClick={() =>
                  setIsOpportunitiesDropdownOpen(!isOpportunitiesDropdownOpen)
                }
              >
                My Opportunities
                <img
                  src="/src/assets/orangeDd.png"
                  alt=""
                  className="w-10 h-auto"
                />
              </button>
            </div>

            {isOpportunitiesDropdownOpen && (
              <div className="flex flex-col items-center w-full">
                <Link
                  to="/faculty-view-all"
                  className={`px-4 py-2 w-full text-center hover:bg-gray-100 ${currentPath === "/applied-opportunities" ? "text-vsu-orange font-bold" : ""}`}
                >
                  Applied
                </Link>

                <Link
                  to="/faculty-view-all"
                  className={`px-4 py-2 w-full text-center hover:bg-gray-100 ${currentPath === "/saved-opportunities" ? "text-vsu-orange font-bold" : ""}`}
                >
                  Saved
                </Link>

                <Link
                  to="/faculty-applicants"
                  className={`px-4 py-2 w-full text-center hover:bg-gray-100 ${currentPath === "/completed-opportunities" ? "text-vsu-orange font-bold" : ""}`}
                >
                  Completed
                </Link>
              </div>
            )}

            <div className="w-full flex flex-col items-center hover:cursor-pointer hover:bg-gray-100">
              <Link
                to="/profile"
                className={`px-4 py-2 w-full text-center ${currentPath === "/profile" ? "text-vsu-orange font-bold" : ""}`}
              >
                Profile
              </Link>
            </div>

          
          </div>
        </div>
      )}
    </div>
  );
};

export default StudentNavBar;