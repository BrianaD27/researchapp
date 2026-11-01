import { useRef } from "react";
import { API_ORIGIN } from "../../api/config";

export default function ProfilePhotoPreview({ src, name }: {
  src: string;
  name: string;
}) {
  const dialog = useRef<HTMLDialogElement>(null);
  const url = new URL(src, API_ORIGIN).href;

  return (
    <>
      <button
        type="button"
        aria-label={`Enlarge photo of ${name}`}
        aria-haspopup="dialog"
        onClick={() => dialog.current?.showModal()}
        className="block text-left mb-4 cursor-zoom-in rounded-lg focus-visible:outline-2 focus-visible:outline-blue-700"
      >
        <img src={url} alt={`Profile of ${name}`}
          className="w-28 h-28 rounded-full object-cover" />
        <span className="block text-sm text-blue-700 underline mt-2">
          View larger photo
        </span>
      </button>
      <dialog
        ref={dialog}
        aria-label={`Photo of ${name}`}
        onClick={(event) => {
          if (event.target === event.currentTarget) dialog.current?.close();
        }}
        className="m-auto w-[90vw] max-w-3xl max-h-[90vh] overflow-auto rounded-xl p-4 bg-white backdrop:bg-black/70"
      >
        <div className="flex justify-between items-center gap-4 mb-3">
          <h2 className="text-lg font-semibold">{name}</h2>
          <button type="button" autoFocus
            onClick={() => dialog.current?.close()}
            className="bg-gray-800 text-white rounded-lg px-4 py-2 cursor-pointer">
            Close
          </button>
        </div>
        <img src={url} alt={`Enlarged profile of ${name}`}
          className="w-full max-h-[70vh] object-contain" />
      </dialog>
    </>
  );
}
