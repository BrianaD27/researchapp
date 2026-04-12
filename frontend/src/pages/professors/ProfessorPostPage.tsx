import FacultyNavBar from "../../components/common/FacultyNavBar";
import React from "react";
import OpportunityEditDetailsCard from "../../components/cards/OpportunityEditDetailsCard";
import MajorInput from "../../components/forms/MajorInput";
import SkillInput from "../../components/forms/SkillInput";

const ProfessorPostPage = () => {
  const [majors, setMajors] = React.useState<string[]>([
    "Computer Science",
    "Engineering",
  ]);
  const [skills, setSkills] = React.useState<string[]>([
    "Python",
    "Machine Learning",
  ]);

  const formatDate = (date: string) => {
    if (!date) return "TBD";
    const [year, month, day] = date.split("-");
    return `${month}/${day}/${year}`;
  }

  const [title, setTitle] = React.useState("OpportunityTitle");
  const [availability, setAvailability] = React.useState("Open");
  const [department, setDepartment] = React.useState("Department Name");
  const [minGpa, setMinGpa] = React.useState("Any");
  const [spots, setSpots] = React.useState("1");
  const [timeCommitment, setTimeCommitment] = React.useState("5");
  const [deadline, setDeadline] = React.useState("");
  const [startDate, setStartDate] = React.useState("");
  const [endDate, setEndDate] = React.useState("");
  const [description, setDescription] = React.useState("This is a description of the research opportunity. It provides details about the project, expectations, and any other relevant information that potential applicants should know.");

  const [selectedClassification, setSelectedClassification] = React.useState<
    string[]
  >([]);
  const classifications = ["Any", "Freshman", "Sophomore", "Junior", "Senior"];
  const availabilityOptions = ["Open", "Closed"];
  const toggleClassification = (classification: string) => {
    setSelectedClassification((prev) =>
      prev.includes(classification)
        ? prev.filter((item) => item !== classification)
        : [...prev, classification],
    );
  };

  return (
    <div className="h-screen bg-[#1B51A4] flex flex-col overflow-scroll">
      <div className="relative z-50">
        <FacultyNavBar />
      </div>

      <div className="pt-5 flex-1 flex items-start justify-evenly">
        <div className="flex flex-col align-items-center justify-start">
          <div className="flex flex-col align-items-center justify-start">
            <p className="text-white/70 mt-5 text-xl pb-2 uppercase">
              Post New Opportunity
            </p>

            <div className="h-159 bg-white rounded-lg px-2 xl:w-190 lg:w-150 md:w-130 w-100 gap-5 mb-3 shadow-lg flex flex-col justify-start">
              {/* Form  */}
              <div className="bg-white py-5 w-full px-5 overflow-scroll h-155 gap-5 flex flex-col justify-start">
                {/* Opportunity Row */}
                <div className="flex flex-col gap-1">
                  <label
                    className="text-sm text-vsu-blue"
                    htmlFor="opportunity-title"
                  >
                    Opportunity Title <span className="text-vsu-orange">*</span>
                  </label>
                  <input
                    className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
                    type="text"
                    id="opportunity-title"
                    required={true}
                    placeholder="Opportunity Title"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
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

                {/* Classification and Availability Row */}
                <div className="flex flex-row flex-wrap justify-between ">
                  <div className="flex flex-col gap-1 ">
                  <label className="text-sm text-vsu-blue">
                    Classification(s) <span className="text-vsu-orange">*</span>
                  </label>
                  <div className="flex flex-row flex-wrap gap-2">
                    {classifications.map((classification) => (
                      <button
                        onClick={() => toggleClassification(classification)}
                        className={`px-4 hover:cursor-pointer text-normal  rounded-full py-1 font-medium border  hover:bg-gray-300 ${selectedClassification.includes(classification) ? "bg-[#E3F1FC] border-[#B7D0E8]" : "bg-gray-100 border-slate-400"}`}
                        key={classification}
                        type="button"
                      >
                        {classification}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="flex flex-col gap-1 ">
                  <label className="text-sm text-vsu-blue">
                    Availability <span className="text-vsu-orange">*</span>
                  </label>
                  <div className="flex flex-row flex-wrap gap-2">
                    {availabilityOptions.map((option) => (
                      <button
                        onClick={() => setAvailability(option)}
                        className={`px-4 hover:cursor-pointer text-normal  rounded-full py-1 font-medium border  hover:bg-gray-300 ${availability === option ? "bg-[#E3F1FC] border-[#B7D0E8]" : "bg-gray-100 border-slate-400"}`}
                        key={option}
                        type="button"
                      >
                        {option}
                      </button>
                    ))}
                  </div>
                </div>

                </div>
                

                {/* GPA, Spots Available Row */}
                <div className="flex flex-row gap-3  ">
                  <div className="flex flex-col gap-1 w-[50%]">
                    <label className="text-sm text-vsu-blue" htmlFor="min-gpa">
                      Min. GPA <span className="text-vsu-orange">*</span>
                    </label>
                    <select
                      className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
                      title="min-gpa"
                      name="min-gpa"
                      id="min-gpa"
                      value={minGpa}
                      onChange={(e) => setMinGpa(e.target.value)}
                    >
                      <option value="any">Any</option>
                      <option value="4">4.0+</option>
                      <option value="3.5">3.5+</option>
                      <option value="3.0">3.0+</option>
                      <option value="2.5">2.5+</option>
                    </select>
                  </div>

                  <div className="flex flex-col gap-1 w-[50%]">
                    <label className="text-sm text-vsu-blue" htmlFor="spots">
                      Spots Available <span className="text-vsu-orange">*</span>
                    </label>
                    <select
                      className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
                      title="spots"
                      name="spots"
                      id="spots"
                      value={spots}
                      onChange={(e) => setSpots(e.target.value)}
                    >
                      {[
                        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
                        17, 18, 19, 20,
                      ].map((num) => (
                        <option value={num} key={num}>
                          {num}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                {/* Time and DeadLine */}
                <div className="flex flex-row gap-3 ">
                  <div className="flex flex-col gap-1 w-[50%]">
                    <label
                      className="text-sm text-vsu-blue"
                      htmlFor="time-commitment"
                    >
                      Time Commitment <span className="text-vsu-orange">*</span>
                    </label>
                    <select
                      className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
                      id="time-commitment"
                      required={true}
                      value={timeCommitment}
                      onChange={(e) => setTimeCommitment(e.target.value)}
                    >
                      <option value="5">5 hrs/week</option>
                      <option value="10">10 hrs/week</option>
                      <option value="15">15 hrs/week</option>
                    </select>
                  </div>

                  <div className="flex flex-col gap-1 w-[50%]">
                    <label
                      className="text-sm text-vsu-blue "
                      htmlFor="application-deadline"
                    >
                      Application Deadline{" "}
                      <span className="text-vsu-orange">*</span>
                    </label>
                    <input
                      type="date"
                      className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
                      id="application-deadline"
                      value={deadline}
                      required={true}
                      onChange={(e) => setDeadline(e.target.value)}
                    />
                  </div>
                </div>

                {/* Duration */}
                <div className="flex flex-row gap-3 ">
                  <div className="flex flex-col gap-1 w-[50%]">
                    <label
                      className="text-sm text-vsu-blue "
                      htmlFor="start-date"
                    >
                      Start Date <span className="text-vsu-orange">*</span>
                    </label>
                    <input
                      type="date"
                      className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
                      id="start-date"
                      required={true}
                      value={startDate}
                      onChange={(e) => setStartDate(e.target.value)}
                    />
                  </div>

                  <div className="flex flex-col gap-1 w-[50%]">
                    <label
                      className="text-sm text-vsu-blue "
                      htmlFor="end-date"
                    >
                      End Date <span className="text-vsu-orange">*</span>
                    </label>
                    <input
                      type="date"
                      className="p-2 bg-gray-100 border border-slate-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4481ba] focus:border-transparent"
                      id="end-date"
                      required={true}
                      value={endDate}
                      onChange={(e) => setEndDate(e.target.value)}
                    />
                  </div>
                </div>

                {/* Target Majors */}
                <div className="flex flex-col gap-1">
                  <label
                    className="text-sm text-vsu-blue "
                    htmlFor="target-majors"
                  >
                    Target Majors <span className="text-vsu-orange">*</span>
                  </label>
                  <MajorInput value={majors} onChange={setMajors} />
                </div>

                {/* Required Skills */}
                <div className="flex flex-col gap-1">
                  <label
                    className="text-sm text-vsu-blue "
                    htmlFor="required-skills"
                  >
                    Required Skills <span className="text-vsu-orange">*</span>
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
                <button className="text-white font-semibold hover:cursor-pointer text-xl bg-green-500 border rounded-3xl py-2 px-8">
                  Submit
                </button>
                <button className="text-white font-semibold hover:cursor-pointer text-xl bg-red-500 border rounded-3xl py-2 px-6">
                  Clear
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* My Opportunity Info Box */}
        <div>
          <OpportunityEditDetailsCard 
            name={title}
            department={department}
            availability={availability}
            commitment={timeCommitment}
            gpa={minGpa}
            spots={spots}
            duration={`${formatDate(startDate)} - ${formatDate(endDate)}`}
            classification={selectedClassification.length ? selectedClassification.join(", ") : "Any"}
            majors={majors}
            skills={skills}
            description={description}
          />
        </div>
      </div>
    </div>
  );
};

export default ProfessorPostPage;
