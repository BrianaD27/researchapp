// ---------------------------------------------------------------------------
// token.ts  —  where the browser remembers who is logged in
// ---------------------------------------------------------------------------
//
// When you log in, the backend hands us a "JWT" (JSON Web Token). Think of it
// as a stamped wristband: every future request to the server shows the wristband
// so the server knows it's really you and what you're allowed to do.
//
// We keep that wristband in the browser's `localStorage`, which is a tiny
// key/value store that survives page refreshes and closing the tab. That way you
// stay logged in until the token expires or you log out.
//
// We store three things:
//   - the access token   ("vsu_token")          -> the wristband, sent on every request
//   - the refresh token   ("vsu_refresh_token") -> used to get a fresh wristband
//                                                  when the old one is about to expire
//   - the username        ("vsu_username")      -> needed by the logout / 2FA endpoints
//
// Every function is wrapped in try/catch because localStorage can throw in some
// browsers (private browsing mode, storage disabled, etc.). If it fails we just
// behave as if nothing is stored.

const TOKEN_KEY = 'vsu_token';
const REFRESH_TOKEN_KEY = 'vsu_refresh_token';
const USERNAME_KEY = 'vsu_username';

// --- access token ----------------------------------------------------------

export const getToken = (): string | null => {
    try {
        return localStorage.getItem(TOKEN_KEY);
    } catch {
        return null;
    }
};

export const setToken = (token: string): void => {
    try {
        localStorage.setItem(TOKEN_KEY, token);
    } catch {
        // storage unavailable (private mode, etc.) - nothing we can do
    }
};

export const clearToken = (): void => {
    try {
        localStorage.removeItem(TOKEN_KEY);
    } catch {
        // ignore
    }
};

// --- refresh token --------------------------------------------------------

export const getRefreshToken = (): string | null => {
    try {
        return localStorage.getItem(REFRESH_TOKEN_KEY);
    } catch {
        return null;
    }
};

export const setRefreshToken = (token: string): void => {
    try {
        localStorage.setItem(REFRESH_TOKEN_KEY, token);
    } catch {
        // ignore
    }
};

// --- username ------------------------------------------------------------

export const getUsername = (): string | null => {
    try {
        return localStorage.getItem(USERNAME_KEY);
    } catch {
        return null;
    }
};

export const setUsername = (username: string): void => {
    try {
        localStorage.setItem(USERNAME_KEY, username);
    } catch {
        // ignore
    }
};

// --- clear everything ---------------------------------------------------

// Call this on logout, or when the server tells us the token is no longer valid.
// It wipes all three values so the app treats you as a logged-out visitor.
export const clearSession = (): void => {
    try {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
        localStorage.removeItem(USERNAME_KEY);
    } catch {
        // ignore
    }
};
