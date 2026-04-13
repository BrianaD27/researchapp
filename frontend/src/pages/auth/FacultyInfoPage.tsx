import React from "react";
import AuthNavBar from "../../components/common/AuthNavBar";
import { useNavigate } from "react-router-dom";

const FacultyInfoPage = () => {
  const [name, setName] = React.useState("");
  const [department, setDepartment] = React.useState("");
  const [email, setEmail] = React.useState("");
  const navigate = useNavigate();

  const [description, setDescription] = React.useState("");

  return (
    <div className="h-screen bg-gray-100 flex flex-col overflow-hidden">
      <div className="relative z-50">
        <AuthNavBar />
      </div>

      <img
        src="/src/assets/studentsBg.png"
        alt=""
        className="flex-1 object-cover"
      />

      <div className="absolute top-[55%] left-1/2 transform -translate-x-1/2 -translate-y-1/2 flex flex-col align-items-center justify-start">
        <div className="flex flex-col align-items-center justify-start">
          <div className="h-150 bg-white rounded-lg px-2 xl:w-230 lg:w-150 md:w-130 w-100 gap-5 mb-3 shadow-lg flex flex-col justify-start">
            {/* Form  */}
            <div className="bg-white py-5 w-full px-5 overflow-scroll h-155 gap-5 flex flex-col justify-start">
              <div className="mt-10">
                <h1 className="text-center font-bold text-3xl text-vsu-orange">
                  Welcome to The Trojan Research Network
                </h1>
                <h1 className="text-center font-medium text-lg text-black">
                  Fill Out the Information Below To Continue
                </h1>
              </div>

              {/* Opportunity Row */}
              <div className="flex flex-col gap-1">
                <label
                  className="text-sm text-vsu-blue"
                  htmlFor="opportunity-title"
                >
                  Name <span className="text-vsu-orange">*</span>
                </label>
                <input
                  className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
                  type="text"
                  id="name"
                  required={true}
                  placeholder="Dr. John Doe"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                />
              </div>

              {/* Email Row */}
              <div className="flex flex-col gap-1">
                <label className="text-sm text-vsu-blue" htmlFor="email">
                  Email <span className="text-vsu-orange">*</span>
                </label>
                <input
                  className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
                  type="email"
                  id="email"
                  required={true}
                  placeholder="dr.johndoe@vsu.edu"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>

              {/* Department Row */}
              <div className="flex flex-col gap-1">
                <label className="text-sm text-vsu-blue" htmlFor="department">
                  Department <span className="text-vsu-orange">*</span>
                </label>
                <input
                  className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
                  type="text"
                  id="department"
                  required={true}
                  placeholder="Department"
                  value={department}
                  onChange={(e) => setDepartment(e.target.value)}
                />
              </div>

              {/* Description */}
              <div className="flex flex-col gap-1">
                <label className="text-sm text-vsu-blue " htmlFor="description">
                  Description <span className="text-vsu-orange">*</span>
                </label>
                <textarea
                  id="description"
                  required={true}
                  value={description}
                  placeholder="Opportunity Description"
                  className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent h-24"
                  onChange={(e) => setDescription(e.target.value)}
                />
              </div>
            </div>

            {/* Buttons */}
            <div className="flex flex-row justify-center items-center mb-4 gap-4">
              {name && email && department && description &&  (<button onClick={() => navigate("/discover-students")} className="text-white font-semibold hover:cursor-pointer text-xl bg-vsu-blue border hover:bg-vsu-blue/60 rounded-3xl py-2 px-8">
                Continue
              </button>)}
              <button className="text-white font-semibold hover:cursor-pointer text-xl bg-red-500 border rounded-3xl py-2 px-6">
                Clear
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default FacultyInfoPage;
