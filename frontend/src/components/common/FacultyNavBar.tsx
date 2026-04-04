import React, { useState } from "react";
import { useLocation, Link } from "react-router-dom";

const FacultyNavBar = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isOpportunitiesDropdownOpen, setIsOpportunitiesDropdownOpen] =
    useState(false);
  const [isProfileDropdownOpen, setIsProfileDropdownOpen] = useState(false);

  const location = useLocation();
  const currentPath = location.pathname;

  const navLinkClass = (path: string) =>
    `nav-link ${currentPath === path ? "text-vsu-orange font-bold border-b-2 border-vsu-orange" : ""}`;

  // Active if on /my-opportunities OR any of its children
  const opportunitiesPaths = ["/my-opportunities", "/faculty-view-all", "/faculty-applicants"];
  const isOpportunitiesActive = opportunitiesPaths.includes(currentPath);

  return (
    <div className=" flex items-center justify-between p-3 px-7 bg-white text-black drop-shadow-lg">
      <div className="flex items-center space-x-3">
        <img
          src="/src/assets/vsuLogo.png"
          alt="VSU Logo"
          className="md:w-28 h-auto w-18"
        />

        <div className="w-0.75 h-14 bg-black"></div>

        <div>
          <h4 className="md:text-lg text-base font-bold">Research</h4>
          <h4 className="md:text-lg text-base font-bold">
            Opportunities Portal
          </h4>
        </div>
      </div>

      {/* PC Menu */}
      <ul className="space-x-4 hidden md:flex">
        <li className="nav-item items-center flex">
          <div className="Dropdown flex items-center cursor-pointer">
            <Link className={navLinkClass("/discover-students")} to="/discover-students">
              Discover Students
            </Link>
          </div>
        </li>

        <li className="nav-item items-center flex">
          <div className="Dropdown flex items-center cursor-pointer">
            <Link className={navLinkClass("/post-new")} to="/post-new">
              Post New
            </Link>
          </div>
        </li>

        <li className="nav-item items-center flex">
          <div className="Dropdown group">
            <div className="Dropdown flex items-center cursor-pointer">
              <Link
                to="/my-opportunities"
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
                to="/faculty-view-all"
                className={`hover:bg-gray-100 pr-4 pl-4 pt-2 pb-2 ${currentPath === "/faculty-view-all" ? "text-vsu-orange font-bold" : ""}`}
              >
                View All
              </Link>
              <Link
                to="/faculty-applicants"
                className={`hover:bg-gray-100 pr-4 pl-4 pt-2 pb-2 ${currentPath === "/faculty-applicants" ? "text-vsu-orange font-bold" : ""}`}
              >
                Applicants
              </Link>
            </div>
          </div>
        </li>

        <li className="nav-item items-center flex">
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
                to="/discover-students"
                className={`px-4 py-2 w-full text-center ${currentPath === "/discover-students" ? "text-vsu-orange font-bold" : ""}`}
              >
                Discover Students
              </Link>
            </div>

            <div className="w-full flex flex-col items-center hover:cursor-pointer hover:bg-gray-100">
              <Link
                to="/post-new"
                className={`px-4 py-2 w-full text-center ${currentPath === "/post-new" ? "text-vsu-orange font-bold" : ""}`}
              >
                Post New
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
                  className={`px-4 py-2 w-full text-center hover:bg-gray-100 ${currentPath === "/faculty-view-all" ? "text-vsu-orange font-bold" : ""}`}
                >
                  View All
                </Link>

                <Link
                  to="/faculty-applicants"
                  className={`px-4 py-2 w-full text-center hover:bg-gray-100 ${currentPath === "/faculty-applicants" ? "text-vsu-orange font-bold" : ""}`}
                >
                  Applicants
                </Link>
              </div>
            )}

            <div className="w-full flex flex-col items-center hover:cursor-pointer hover:bg-gray-100">
              <button
                className="Dropdown flex items-center cursor-pointer"
                onClick={() => setIsProfileDropdownOpen(!isProfileDropdownOpen)}
              >
                Profile
                <img
                  src="/src/assets/orangeDd.png"
                  alt=""
                  className="w-10 h-auto"
                />
              </button>
            </div>

            {isProfileDropdownOpen && (
              <div className="flex flex-col items-center w-full">
                <Link
                  to="/faculty-account"
                  className={`px-4 py-2 w-full text-center hover:bg-gray-100 ${currentPath === "/faculty-account" ? "text-vsu-orange font-bold" : ""}`}
                >
                  Account
                </Link>

                <Link
                  to="/faculty-settings"
                  className={`px-4 py-2 w-full text-center hover:bg-gray-100 ${currentPath === "/faculty-settings" ? "text-vsu-orange font-bold" : ""}`}
                >
                  Settings
                </Link>

                <Link
                  to="/faculty-login"
                  className="px-4 py-2 w-full text-center text-red-500 hover:bg-gray-100"
                >
                  Logout
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default FacultyNavBar;