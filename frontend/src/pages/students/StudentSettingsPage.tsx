import { useState } from "react";
import FacultyNavBar from "../../components/common/FacultyNavBar";
import SkillInput from "../../components/forms/SkillInput";
const StudentsSettingsPage = () => {
  const options = [
    "Change Name",
    "Change Major",
    "Change Classification",
    "Change Graduation Year",
    "Change Email",
    "Update GPA",
    "Update Available Hours",
    "Update Experience",
    "Update Skills",
    "Update Resume",
    "Update Profile Picture",
    "Update Profile Description",
  ];
  const [selectedOption, setSelectedOption] = useState<string>("name");
  const [name, setName] = useState<string>("");
  const [email, setEmail] = useState<string>("");
  const [major, setMajor] = useState<string>("");
  const [classification, setClassification] = useState<string>("");
  const classificationOptions = ["Freshman", "Sophomore", "Junior", "Senior"];
  const [graduationYear, setGraduationYear] = useState<string>("");
  const [availability, setAvailability] = useState<string>("");
  const [experience, setExperience] = useState<string>("");
  const [skills, setSkills] = useState<string[]>([]);
  const [resumeUrl, setResumeUrl] = useState<string>("");
  const [resumeFile, setResumeFile] = useState<File | null>(null);
  const handleResumeUpload = async () => {
    if (!resumeFile) return;

    const resumeData = new FormData();
    resumeData.append("resume", resumeFile);

    // TODO: Implement API call to upload resume
  };
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
    "Change Major": (
      <div className="w-full p-4 flex flex-row items-end gap-5 ">
        <div className="flex flex-col gap-1 w-[50%]">
          <label className="text-sm text-vsu-blue" htmlFor="opportunity-title">
            New Major
          </label>
          <input
            className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
            type="text"
            id="major"
            required={true}
            placeholder="New Major"
            value={major}
            onChange={(e) => setMajor(e.target.value)}
          />
        </div>
        <button className="px-4 hover:cursor-pointer py-2 bg-green-500 text-white rounded-lg hover:bg-green-700 transition-colors duration-300">
          Save
        </button>
      </div>
    ),
    "Change Classification": (
      <div className="w-full p-4 flex flex-row items-end gap-5 ">
        <div className="flex flex-col gap-1 ">
          <label className="text-sm text-vsu-blue">
            Availability <span className="text-vsu-orange">*</span>
          </label>
          <div className="flex flex-row flex-wrap gap-2">
            {classificationOptions.map((option) => (
              <button
                onClick={() => setClassification(option)}
                className={`px-4 hover:cursor-pointer text-normal  rounded-full py-1 font-medium border  hover:bg-gray-300 ${classification === option ? "bg-[#E3F1FC] border-[#B7D0E8]" : "bg-gray-100 border-slate-400"}`}
                key={option}
                type="button"
              >
                {option}
              </button>
            ))}
          </div>
        </div>
        <button className="px-4 hover:cursor-pointer py-2 bg-green-500 text-white rounded-lg hover:bg-green-700 transition-colors duration-300">
          Save
        </button>
      </div>
    ),
    "Change Graduation Year": (
      <div className="w-full p-4 flex flex-row items-end gap-5 ">
        <div className="flex flex-col gap-1 w-[50%]">
          <label className="text-sm text-vsu-blue" htmlFor="spots">
            Graduation Year <span className="text-vsu-orange">*</span>
          </label>
          <select
            className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
            title="graduationYear"
            name="graduationYear"
            id="graduationYear"
            value={graduationYear}
            onChange={(e) => setGraduationYear(e.target.value)}
          >
            {Array.from({ length: 2080 - 2026 + 1 }, (_, i) => 2026 + i).map(
              (num) => (
                <option value={num} key={num}>
                  {num}
                </option>
              ),
            )}
          </select>
        </div>
        <button className="px-4 hover:cursor-pointer py-2 bg-green-500 text-white rounded-lg hover:bg-green-700 transition-colors duration-300">
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
    "Update GPA": (
      <div className="w-full p-4 flex flex-row items-end gap-5 ">
        <div className="flex flex-col gap-1 w-[50%]">
          <label className="text-sm text-vsu-blue" htmlFor="spots">
            GPA <span className="text-vsu-orange">*</span>
          </label>
          <select
            className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
            title="graduationYear"
            name="graduationYear"
            id="graduationYear"
            value={graduationYear}
            onChange={(e) => setGraduationYear(e.target.value)}
          >
            {Array.from({ length: 41 }, (_, i) =>
              parseFloat((i * 0.1).toFixed(1)),
            ).map((num) => (
              <option value={num} key={num}>
                {num}
              </option>
            ))}
          </select>
        </div>
        <button className="px-4 hover:cursor-pointer py-2 bg-green-500 text-white rounded-lg hover:bg-green-700 transition-colors duration-300">
          Save
        </button>
      </div>
    ),
    "Update Available Hours": (
      <div className="w-full p-4 flex flex-row items-end gap-5 ">
        <div className="flex flex-col gap-1 w-[50%]">
          <label className="text-sm text-vsu-blue" htmlFor="time-commitment">
            Availability <span className="text-vsu-orange">*</span>
          </label>
          <select
            className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
            id="availability"
            title="availability"
            value={availability}
            onChange={(e) => setAvailability(e.target.value)}
          >
            <option value="5">5 hrs/week</option>
            <option value="10">10 hrs/week</option>
            <option value="15">15 hrs/week</option>
          </select>
        </div>
        <button className="px-4 hover:cursor-pointer py-2 bg-green-500 text-white rounded-lg hover:bg-green-700 transition-colors duration-300">
          Save
        </button>
      </div>
    ),
    "Update Experience": (
      <div className="w-full p-4 flex flex-row items-end gap-5 ">
        <div className="flex flex-col gap-1 w-[50%]">
          <label className="text-sm text-vsu-blue" htmlFor="time-commitment">
            Experience <span className="text-vsu-orange">*</span>
          </label>
          <select
            className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
            id="experience"
            title="experience"
            value={experience}
            onChange={(e) => setExperience(e.target.value)}
          >
            <option value="yes">Yes</option>
            <option value="no">No</option>
          </select>
        </div>
        <button className="px-4 hover:cursor-pointer py-2 bg-green-500 text-white rounded-lg hover:bg-green-700 transition-colors duration-300">
          Save
        </button>
      </div>
    ),
    "Update Skills": (
      <div className="w-full p-4 flex flex-row items-end gap-5 ">
        <div className="flex flex-col gap-1 w-[70%]">
          <label className="text-sm text-vsu-blue " htmlFor="required-skills">
            Required Skills <span className="text-vsu-orange">*</span>
          </label>
          <div>
            <SkillInput value={skills} onChange={setSkills} />
          </div>
        </div>
        <button className="px-4 hover:cursor-pointer py-2 bg-green-500 text-white rounded-lg hover:bg-green-700 transition-colors duration-300">
          Save
        </button>
        {skills.length > 0 && (
          <button className="px-4 hover:cursor-pointer py-2 bg-red-500 text-white rounded-lg hover:bg-red-700 transition-colors duration-300">
            Clear SKills
          </button>
        )}
      </div>
    ),
    "Update Resume": (
      <div className="w-full p-4 flex flex-row items-end gap-5 ">
        <div className="flex flex-col gap-1 w-[50%]">
          <label className="text-sm text-vsu-blue" htmlFor="opportunity-title">
            Upload Resume
          </label>
          <div className="flex flex-row items-center gap-5">
            <input
              className="p-2 bg-gray-100 border border-slate-400 rounded-lg hover:cursor-pointer focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
              type="file"
              accept=".pdf,.doc,.docx"
              id="resume"
              title="resume"
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (file) {
                  const url = URL.createObjectURL(file);
                  setResumeUrl(url);
                  setResumeFile(file);
                }
              }}
            />
            <button
              className="px-4 hover:cursor-pointer py-2 bg-green-500 text-white rounded-lg hover:bg-green-700 transition-colors duration-300 "
              onClick={handleResumeUpload}
            >
              Update Resume
            </button>

            {resumeUrl && (
              <button
                className="px-4 hover:cursor-pointer py-2 bg-red-500 text-white rounded-lg hover:bg-red-700 transition-colors duration-300"
                onClick={() => {
                  setResumeUrl("");
                  setResumeFile(null);
                  const input = document.getElementById(
                    "resume",
                  ) as HTMLInputElement;
                  if (input) {
                    input.value = "";
                  }
                }}
              >
                Remove
              </button>
            )}
          </div>
        </div>
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
              <a href="/FacultyLogin" className="text-red-500 font-semibold">
                Log Out
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StudentsSettingsPage;
