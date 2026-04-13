import { useState } from "react";
import FacultyNavBar from "../../components/common/FacultyNavBar";
const ProfessorProfilePage = () => {
  const options = [
    "Change Name",
    "Change Department",
    "Change Email",
    "Update Profile Picture",
    "Update Profile Description",
  ];
  const [selectedOption, setSelectedOption] = useState<string>("name");
  const [name, setName] = useState<string>("");
  const [email, setEmail] = useState<string>("");
  const [department, setDepartment] = useState<string>("");
  const [description, setDescription] = useState<string>("");
  const [profilePicture, setProfilePicture] = useState<string>("");
  const optionsContent: Record<string, React.ReactNode> = {
    "Change Name": (
      <div className="w-full p-4 flex flex-row items-end gap-5 ">
        <div className="flex flex-col gap-1 w-[50%]">
          <label className="text-sm text-vsu-blue" htmlFor="opportunity-title">
            New Name
          </label>
          <input
            className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
            type="text"
            id="name"
            required={true}
            placeholder="Opportunity Title"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </div>
        <button className="px-4 hover:cursor-pointer py-2 bg-green-500 text-white rounded-lg hover:bg-green-700 transition-colors duration-300">
          Save
        </button>
      </div>
    ),
    "Change Department": (
      <div className="w-full p-4 flex flex-row items-end gap-5 ">
        <div className="flex flex-col gap-1 w-[50%]">
          <label className="text-sm text-vsu-blue" htmlFor="opportunity-title">
            New Department
          </label>
          <input
            className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
            type="text"
            id="department"
            required={true}
            placeholder="CompSci, Math, etc."
            value={department}
            onChange={(e) => setDepartment(e.target.value)}
          />
        </div>
        <button className="hover:cursor-pointer px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-700 transition-colors duration-300">
          Save
        </button>
      </div>
    ),
    "Change Email": (
      <div className="w-full p-4 flex flex-row items-end gap-5 ">
        <div className="flex flex-col gap-1 w-[50%]">
          <label className="text-sm text-vsu-blue" htmlFor="opportunity-title">
            New Email
          </label>
          <input
            className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
            type="email"
            id="email"
            required={true}
            placeholder="new.email@vsu.edu"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>
        <button className="hover:cursor-pointer px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-700 transition-colors duration-300">
          Save
        </button>
      </div>
    ),
    "Update Profile Picture": (
      <div className="w-full p-4 flex flex-row items-center justify-start gap-5">
        {/* Circle Display */}
        <div className="border-1 border-black rounded-full w-50 h-50 bg-gray-800">
          {profilePicture ? (
            <img
              src={profilePicture}
              alt="Profile"
              className="w-full h-full object-cover rounded-full"
            />
          ) : (
            <p className="w-full h-full flex items-center justify-center text-white">
              No Image
            </p>
          )}
        </div>

        {/* File Input */}
        <div className="flex flex-col gap-1 w-[50%]">
          <label className="text-sm text-vsu-blue" htmlFor="opportunity-title">
            Upload Photo
          </label>
          <div className="flex flex-row items-center gap-5">
            <input
              className="p-2 bg-gray-100 border border-slate-400 rounded-lg hover:cursor-pointer focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
              type="file"
              accept="image/*"
              id="picture"
              title="picture"
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (file) {
                  const url = URL.createObjectURL(file);
                  setProfilePicture(url);
                }
              }}
            />
            <button className="px-4 hover:cursor-pointer py-2 bg-green-500 text-white rounded-lg hover:bg-green-700 transition-colors duration-300 ">
              Update Photo
            </button>

            {profilePicture && (
          <button className="px-4 hover:cursor-pointer py-2 bg-red-500 text-white rounded-lg hover:bg-red-700 transition-colors duration-300">
            Remove
          </button>
        )}
          </div>
        </div>

        
      </div>
    ),
    "Update Profile Description": (
      <div className="w-full p-4 flex flex-row items-end gap-5 ">
        <div className="flex flex-col gap-1 w-[50%]">
          <label className="text-sm text-vsu-blue" htmlFor="opportunity-title">
            New Description
          </label>
          <textarea
            className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
            id="description"
            required={true}
            placeholder="New description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>
        <button className="hover:cursor-pointer px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-700 transition-colors duration-300">
          Save
        </button>
      </div>
    ),
  };

  return (
    <div className="h-screen bg-[#1B51A4] flex flex-col overflow-scroll">
      <div className="relative z-50">
        <FacultyNavBar />
      </div>

      <div className="flex-1 my-10 mx-10 rounded-2xl items-center justify-center overflow-scroll">
        <div>
          <p className="text-white/70 text-xl pb-2 uppercase">
            Account Settings
          </p>
          <div className="flex h-auto justify-start p-8 items-start rounded-lg bg-white">
            <div className="flex flex-col items-start w-full">
              {options.map((option) => (
                <div className="w-full flex flex-col items-start " key={option}>
                  <div
                    onClick={() => {
                      if (selectedOption === option) {
                        setSelectedOption("");
                      } else {
                        setSelectedOption(option);
                      }
                    }}
                    className="flex flex-col items-start hover:bg-gray-100 cursor-pointer transition-colors duration-300 pt-5 w-full gap-5"
                  >
                    <button className="cursor-pointer">{option}</button>
                    <div className="w-full border-b-2 border-slate-200"></div>
                  </div>

                  {selectedOption === option && optionsContent[option]}
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="my-5">
          <p className="text-white/70 text-xl pb-2 uppercase">Exit</p>
          <div className="flex h-auto justify-start p-8 items-start rounded-lg bg-white">
            <div className="flex flex-col items-start w-full gap-5">
              <a href="/FacultyLogin" className="text-red-500 font-semibold">Log Out</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfessorProfilePage;
