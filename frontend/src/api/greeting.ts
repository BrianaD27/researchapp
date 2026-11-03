// "Good morning" / "Good afternoon" / "Good evening" for the person's local time.
export function timeOfDay(hour: number) {
  if (hour < 12) return "Good morning";
  if (hour < 17) return "Good afternoon";
  return "Good evening";
}
