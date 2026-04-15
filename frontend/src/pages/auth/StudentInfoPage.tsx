import React, { useState } from "react";
import AuthNavBar from "../../components/common/AuthNavBar";
import SkillInput from "../../components/forms/SkillInput";
import { useNavigate } from "react-router-dom";

const StudentInfoPage = () => {
  const [skills, setSkills] = React.useState<string[]>([
    "Python",
    "Machine Learning",
  ]);

  const [name, setName] = React.useState("");
  const [availability, setAvailability] = React.useState("Open");
  const [experience, setExperience] = React.useState("yes");
  const experienceOptions = ["Yes", "No"];
  const [email, setEmail] = React.useState("");
  const [major, setMajor] = React.useState("");
  const [gpa, setGpa] = React.useState("Any");
  const [graduationYear, setGraduationYear] = React.useState("");
  const [resumeUrl, setResumeUrl] = useState<string>("");
  const [resumeFile, setResumeFile] = useState<File | null>(null);
    const handleResumeUpload = async () => {
      if (!resumeFile) return;

      const resumeData = new FormData();
      resumeData.append("resume", resumeFile);
      console.log("Resume url ready for upload:", resumeUrl); // Line not needed. Remove later

      // TODO: Implement API call to upload resume
    };

  const [description, setDescription] = React.useState("");

  const [classification, setClassification] = React.useState("");
  const classificationOptions = ["Freshman", "Sophomore", "Junior", "Senior"];

  const navigate = useNavigate();

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
          <div className="flex flex-col align-items-center justify-start">
            <div className="h-159 bg-white rounded-lg px-2 xl:w-190 lg:w-150 md:w-130 w-100 gap-5 mb-3 shadow-lg flex flex-col justify-start">
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

                {/* Name Row */}
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
                    placeholder="Name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                  />
                </div>

                {/* Major Row */}
                <div className="flex flex-col gap-1">
                  <label className="text-sm text-vsu-blue" htmlFor="major">
                    Major <span className="text-vsu-orange">*</span>
                  </label>
                  <input
                    className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
                    type="text"
                    id="major"
                    required={true}
                    placeholder="Major"
                    value={major}
                    onChange={(e) => setMajor(e.target.value)}
                  />
                </div>

                {/* Email Row */}
                <div className="flex flex-col gap-1">
                  <label className="text-sm text-vsu-blue" htmlFor="email">
                    Email <span className="text-vsu-orange">*</span>
                  </label>
                  <input
                    className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
                    type="text"
                    id="email"
                    required={true}
                    placeholder="Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                  />
                </div>

                {/* Classification and Experience Row */}
                <div className="flex flex-row flex-wrap justify-between ">
                  <div className="flex flex-col gap-1 w-auto">
                    <label className="text-sm text-vsu-blue">
                      Classification <span className="text-vsu-orange">*</span>
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

                  <div className="flex flex-col gap-1 w-auto">
                    <label className="text-sm text-vsu-blue">
                      Previous Experience{" "}
                      <span className="text-vsu-orange">*</span>
                    </label>
                    <div className="flex flex-row flex-wrap gap-2">
                      {experienceOptions.map((option) => (
                        <button
                          onClick={() => setExperience(option)}
                          className={`px-4 hover:cursor-pointer text-normal  rounded-full py-1 font-medium border  hover:bg-gray-300 ${experience === option ? "bg-[#E3F1FC] border-[#B7D0E8]" : "bg-gray-100 border-slate-400"}`}
                          key={option}
                          type="button"
                        >
                          {option}
                        </button>
                      ))}
                    </div>
                  </div>
                </div>

                {/* GPA, Graduation Year Row */}
                <div className="flex flex-row gap-3  ">
                  <div className="flex flex-col gap-1 w-[50%]">
                    <label className="text-sm text-vsu-blue" htmlFor="spots">
                      GPA <span className="text-vsu-orange">*</span>
                    </label>
                    <select
                      className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
                      title="gpa"
                      name="gpa"
                      id="gpa"
                      value={gpa}
                      onChange={(e) => setGpa(e.target.value)}
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
                      {Array.from(
                        { length: 2080 - 2026 + 1 },
                        (_, i) => 2026 + i,
                      ).map((num) => (
                        <option value={num} key={num}>
                          {num}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                {/* Availability and Resume */}
                <div className="flex flex-row gap-3 ">
                  <div className="flex flex-col gap-1 w-[50%]">
                    <label
                      className="text-sm text-vsu-blue"
                      htmlFor="time-commitment"
                    >
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

                  <div className="flex flex-col gap-1 w-[50%]">
                    <label
                      className="text-sm text-vsu-blue"
                      htmlFor="opportunity-title"
                    >
                      Upload Resume
                    </label>
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
                  </div>
                </div>

                {/* Users Skills */}
                <div className="flex flex-col gap-1">
                  <label
                    className="text-sm text-vsu-blue "
                    htmlFor="required-skills"
                  >
                    Skills <span className="text-vsu-orange">*</span>
                  </label>
                  <div>
                    <SkillInput value={skills} onChange={setSkills} />
                  </div>
                </div>

                {/* Description */}
                <div className="flex flex-col gap-1">
                  <label
                    className="text-sm text-vsu-blue "
                    htmlFor="description"
                  >
                    Description <span className="text-vsu-orange">*</span>
                  </label>
                  <textarea
                    id="description"
                    required={true}
                    value={description}
                    placeholder="Opportunity Description"
                    className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent h-24 resize-none"
                    onChange={(e) => setDescription(e.target.value)}
                  />
                </div>
              </div>

              {/* Buttons */}
              <div className="flex flex-row justify-center items-center mb-4 gap-4">
                <button
                  onClick={() => {
                    navigate("/discover-opportunities");
                    handleResumeUpload();

                  }}
                  className="text-white font-semibold hover:cursor-pointer text-xl bg-vsu-blue hover:bg-vsu-blue/60 border rounded-3xl py-2 px-8"
                >
                  Continue
                </button>
                <button className="text-white font-semibold hover:cursor-pointer text-xl bg-red-500 border rounded-3xl py-2 px-6">
                  Clear
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StudentInfoPage;
