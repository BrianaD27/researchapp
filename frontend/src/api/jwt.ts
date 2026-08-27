// ---------------------------------------------------------------------------
// jwt.ts  —  reading the information stored inside a login token
// ---------------------------------------------------------------------------
//
// A JWT looks like three chunks separated by dots:  header.payload.signature
// The middle chunk ("payload") is just JSON that has been Base64-encoded. It is
// NOT encrypted - anyone can read it - so we can safely decode it in the browser
// to find out who the token belongs to and when it expires.
//
// We only trust this for display / routing decisions. The real security check
// happens on the backend, which verifies the signature on every request.

export interface DecodedToken {
    // "sub" (subject) is the standard JWT field for "who this token is about".
    // Our backend puts the username here.
    sub: string;
    // Our backend adds a custom "role" field: "STUDENT", "PROFESSOR" or "ADMIN".
    role: string;
    // "exp" is the expiry time, in SECONDS since 1970 (Unix time).
    exp: number;
}

// Decode the payload of a JWT. Returns null if the token is missing or malformed.
export const decodeToken = (token: string | null): DecodedToken | null => {
    if (!token) return null;

    try {
        // Grab the middle section (index 1) between the dots.
        const payload = token.split('.')[1];
        if (!payload) return null;

        // JWTs use "base64url" encoding, which swaps a couple of characters
        // compared to normal base64. Swap them back before decoding.
        const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');

        // atob() turns a base64 string into raw text; JSON.parse turns it into
        // a real object.
        const json = JSON.parse(atob(base64));

        return {
            sub: json.sub,
            role: json.role,
            exp: json.exp,
        };
    } catch {
        // Anything unexpected -> treat the token as unreadable.
        return null;
    }
};

// Convenience check: is this token already past its expiry time?
export const isTokenExpired = (token: string | null): boolean => {
    const decoded = decodeToken(token);
    if (!decoded?.exp) return true;

    // Date.now() is in milliseconds; exp is in seconds -> multiply by 1000.
    return Date.now() >= decoded.exp * 1000;
};
