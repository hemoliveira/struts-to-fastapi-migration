export function formatDate(isoString) {
  if (!isoString) return ''
  // Normalize the API's ISO datetime ("2026-07-06T20:12:34.567890") to the
  // same "YYYY-MM-DD HH:MM:SS" shape the legacy JSP view shows (Java's
  // java.sql.Timestamp#toString), so the two apps read the same way
  // side by side.
  return isoString.replace('T', ' ').split('.')[0]
}
