import { Link } from "react-router-dom";

// Header for partner-school visitors (view only).
export default function PartnerNavBar() {
  return (
    <div className="flex items-center justify-between p-3 px-7 bg-white text-black drop-shadow-lg">
      <Link to="/partner-research" title="VSU research" aria-label="VSU research"
        className="flex items-center space-x-3 cursor-pointer">
        <img src="/src/assets/vsuLogo.png" alt="VSU Logo" className="lg:w-24 h-auto w-16" />
        <div className="w-0.75 h-14 bg-black"></div>
        <div>
          <h4 className="lg:text-lg text-sm font-bold">Trojan Partner</h4>
          <h4 className="lg:text-lg text-sm font-bold">Research Network</h4>
        </div>
      </Link>
      <Link to="/partner-research" className="text-sm lg:text-base font-semibold text-vsu-orange">
        VSU Research
      </Link>
    </div>
  );
}
