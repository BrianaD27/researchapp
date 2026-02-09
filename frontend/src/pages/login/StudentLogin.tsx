import React from "react";
import NavBar from "../../components/common/NavBar";

const StudentLogin = () => {
  return (
    <div className="h-screen bg-gray-100 flex flex-col">
      <NavBar />

    
      <img
        src="/src/assets/studentsBg.png"
        alt=""
        className="flex-1 object-cover"
      />
      <div className=""></div>
    </div>
  );
};

export default StudentLogin;
