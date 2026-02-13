import React from "react";
import { useNavigate } from "react-router-dom";

interface AuthFormProps {
  title: string;
  textFields: { label: string; type: string; id: string }[];
  buttonText: string;
  buttonUrl: string;
}

const AuthForm: React.FC<AuthFormProps> = ({
  title,
  textFields,
  buttonText,
  buttonUrl
}) => {
  const navigate = useNavigate();
  return (
    <div>
      <div className="absolute bg-white md:w-108 w-84 h-auto rounded-lg shadow-lg top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 z-10">
        <div className="p-4 flex flex-col justify-center items-center h-full my-2">
          <img
            src="/src/assets/vsuCircleLogo.png"
            alt=""
            className="h-20 w-auto object-cover"
          />
          <h2 className="text-center text-4xl font-bold text-vsu-orange">
            {title}
          </h2>
          <p className="text-center text-lg font-medium text-black mt-1">
            Bridging VSU talent with research opportunities
          </p>

          <form className="mt-4 mb-8 px-5">
            <div className="">
              {textFields.map((field) => (
                <>
                  <label htmlFor={field.id}>{field.label}</label>
                  <input
                    id={field.id}
                    type={field.type}
                    className="w-full mb-3 p-1 border-black bg-gray-100 border-2"
                  />
                </>
              ))}
            </div>
          </form>

          <button onClick={() => navigate(buttonUrl)} className="bg-vsu-orange text-white px-8 py-2 rounded hover:bg-orange-600 text-xl font-semibold">
            {buttonText}
          </button>
        </div>
      </div>
    </div>
  );
};

export default AuthForm;
