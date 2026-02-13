import React, { useState } from "react";

const NavBar = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isFacultyDropdownOpen, setIsFacultyDropdownOpen] = useState(false);
  const [isStudentDropdownOpen, setIsStudentDropdownOpen] = useState(false);

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
      <ul className="flex space-x-4 hidden md:flex">
        <li className="nav-item items-center flex">
          <div className="Dropdown group">
            <div className="Dropdown flex items-center cursor-pointer">
              <a className="nav-link" href="">
                Faculty
              </a>
              <img
                src="/src/assets/orangeDd.png"
                alt=""
                className="w-10 h-auto"
              />
            </div>

            <div className="dropdown-content hidden group-hover:flex flex-col absolute bg-white border border-gray-300">
              <a href="/FacultyLogin" className="hover:bg-gray-100 pr-4 pl-4 pt-2 pb-2">
                Log In
              </a>
              <a href="/FacultySignUp" className="hover:bg-gray-100 pr-4 pl-4 pt-2 pb-2">
                Sign Up
              </a>
            </div>
          </div>
        </li>

        <li className="nav-item items-center flex">
          <div className="Dropdown group">
            <div className="Dropdown flex items-center cursor-pointer">
              <a className="nav-link" href="/faculty-login">
                Student
              </a>
              <img
                src="/src/assets/orangeDd.png"
                alt=""
                className="w-10 h-auto"
              />
            </div>

            <div className="dropdown-content hidden group-hover:flex flex-col absolute bg-white border border-gray-300">
              <a href="/StudentLogin" className="hover:bg-gray-100 pr-4 pl-4 pt-2 pb-2">
                Log In
              </a>
              <a href="/StudentSignUp" className="hover:bg-gray-100 pr-4 pl-4 pt-2 pb-2">
                Sign Up
              </a>
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
              <button
                className="Dropdown flex items-center cursor-pointer"
                onClick={() => setIsFacultyDropdownOpen(!isFacultyDropdownOpen)}
              >
                Faculty
                <img
                  src="/src/assets/orangeDd.png"
                  alt=""
                  className="w-10 h-auto"
                />
              </button>
            </div>

            {isFacultyDropdownOpen && (
              <div className="flex flex-col items-center w-full">
                <a
                  href="/"
                  className="px-4 py-2 w-full text-center hover:bg-gray-100"
                >
                  Log In
                </a>

                <a
                  href="/"
                  className="px-4 py-2 w-full text-center hover:bg-gray-100"
                >
                  Sign Up
                </a>
              </div>
            )}

            <div className="w-full flex flex-col items-center hover:cursor-pointer hover:bg-gray-100">
              <button
                className="Dropdown flex items-center cursor-pointer"
                onClick={() => setIsStudentDropdownOpen(!isStudentDropdownOpen)}
              >
                Students
                <img
                  src="/src/assets/orangeDd.png"
                  alt=""
                  className="w-10 h-auto"
                />
              </button>
            </div>

            {isStudentDropdownOpen && (
              <div className="flex flex-col items-center w-full">
                <a
                  href="/"
                  className="px-4 py-2 w-full text-center hover:bg-gray-100"
                >
                  Log In
                </a>

                <a
                  href="/"
                  className="px-4 py-2 w-full text-center hover:bg-gray-100"
                >
                  Sign Up
                </a>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default NavBar;
