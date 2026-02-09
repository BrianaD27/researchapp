import React from "react";

const NavBar = () => {
  return (
    <div className=" flex items-center justify-between p-3 bg-white text-black drop-shadow-lg">
      <div className="flex items-center space-x-3">
        <img
          src="/src/assets/vsuLogo.png"
          alt="VSU Logo"
          className="w-28 h-auto"
        />

        <div className="w-0.75 h-14 bg-black"></div>

        <div>
          <h4 className="text-lg font-bold">Research</h4>
          <h4 className="text-lg font-bold">Opportunities Portal</h4>
        </div>
      </div>

      <ul className="flex space-x-4">
        <li className="nav-item items-center flex">
          <div className="Dropdown group">
            <div className="Dropdown flex items-center cursor-pointer">
              <a className="nav-link" href="/faculty-login">
                Faculty
              </a>
              <img
                src="/src/assets/orangeDd.png"
                alt=""
                className="w-10 h-auto"
              />
            </div>

            <div className="dropdown-content hidden group-hover:flex flex-col absolute bg-white border border-gray-300">
              <a href="/" className="hover:bg-gray-100 pr-4 pl-4 pt-2 pb-2">
                Log In
              </a>
              <a href="/" className="hover:bg-gray-100 pr-4 pl-4 pt-2 pb-2">
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
              <a href="/" className="hover:bg-gray-100 pr-4 pl-4 pt-2 pb-2">
                Log In
              </a>
              <a href="/" className="hover:bg-gray-100 pr-4 pl-4 pt-2 pb-2">
                Sign Up
              </a>
            </div>
          </div>
        </li>

        <li className="nav-item items-center flex">
          <div className="Dropdown group">
            <div className="Dropdown flex items-center cursor-pointer">
              <a className="nav-link" href="/faculty-login">
                Admin
              </a>
              <img
                src="/src/assets/orangeDd.png"
                alt=""
                className="w-10 h-auto"
              />
            </div>

            <div className="dropdown-content hidden group-hover:flex flex-col absolute bg-white border border-gray-300">
              <a href="/" className="hover:bg-gray-100 pr-4 pl-4 pt-2 pb-2">
                Log In
              </a>
              <a href="/" className="hover:bg-gray-100 pr-4 pl-4 pt-2 pb-2">
                Sign Up
              </a>
            </div>
          </div>
        </li>
      </ul>
    </div>
  );
};

export default NavBar;
