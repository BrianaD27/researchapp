// ---------------------------------------------------------------------------
// AuthForm.tsx  —  the shared white card used by every login / signup screen
// ---------------------------------------------------------------------------
//
// This component only knows how to draw a form and collect what the user types.
// It does NOT know anything about the API. Each page (StudentLogin, FacultySignUp,
// etc.) passes in:
//   - `fields`     : which inputs to show
//   - `submitText` : the button label
//   - `onSubmit`   : an async function that receives the typed values and does
//                    the real work (call the API, navigate, etc.)
//
// "CONTROLLED INPUTS": in React, an <input> that shows the value from state and
// updates that state on every keystroke is called "controlled". That's what lets
// us actually read what the user typed when they submit.

import { useState, type FormEvent } from 'react';

export interface AuthFormField {
    label: string;
    type: string; // "text" | "email" | "password" | "number" ...
    id: string; // also used as the key in the values object, e.g. "username"
}

interface AuthFormProps {
    title: string;
    fields: AuthFormField[];
    submitText: string;
    // Called when the form is submitted. If it throws, we show the error message.
    onSubmit: (values: Record<string, string>) => Promise<void> | void;
    // Optional extra content under the button (e.g. a "Sign up instead" link).
    footer?: React.ReactNode;
}

const AuthForm = ({ title, fields, submitText, onSubmit, footer }: AuthFormProps) => {
    // One state object holding every field's current text, keyed by field id:
    //   { username: "steve", password: "hunter2" }
    const [values, setValues] = useState<Record<string, string>>({});
    // While onSubmit is running we disable the button so it can't be double-clicked.
    const [loading, setLoading] = useState(false);
    // Any error message to show the user in red.
    const [error, setError] = useState<string | null>(null);

    // Update one field as the user types.
    const handleChange = (id: string, value: string) => {
        setValues((prev) => ({ ...prev, [id]: value }));
    };

    const handleSubmit = async (e: FormEvent) => {
        // Stop the browser's default behaviour (a full page reload).
        e.preventDefault();
        setError(null);
        setLoading(true);
        try {
            await onSubmit(values);
        } catch (err: unknown) {
            // Try to pull a helpful message out of an axios error, otherwise
            // fall back to something generic.
            const message =
                // axios puts the server's JSON body on err.response.data
                (err as { response?: { data?: { message?: string } } })?.response?.data
                    ?.message ??
                (err as Error)?.message ??
                'Something went wrong. Please try again.';
            setError(message);
        } finally {
            setLoading(false);
        }
    };

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

                    <form className="mt-4 mb-8 px-5 w-full" onSubmit={handleSubmit}>
                        {fields.map((field) => (
                            <div key={field.id}>
                                <label htmlFor={field.id}>{field.label}</label>
                                <input
                                    id={field.id}
                                    type={field.type}
                                    value={values[field.id] ?? ''}
                                    onChange={(e) => handleChange(field.id, e.target.value)}
                                    className="w-full mb-3 p-1 border-black bg-gray-100 border-2"
                                />
                            </div>
                        ))}

                        {/* Error message, only shown when there is one */}
                        {error && (
                            <p className="text-red-600 text-sm mb-3 text-center">{error}</p>
                        )}

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full bg-vsu-orange text-white px-8 py-2 rounded hover:bg-orange-600 text-xl font-semibold disabled:opacity-60"
                        >
                            {loading ? 'Please wait…' : submitText}
                        </button>
                    </form>

                    {footer && <div className="mb-4 text-sm">{footer}</div>}
                </div>
            </div>
        </div>
    );
};

export default AuthForm;
