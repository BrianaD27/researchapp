// ---------------------------------------------------------------------------
// AuthContext.tsx  —  the app's single source of truth for "who is logged in"
// ---------------------------------------------------------------------------
//
// WHAT IS "CONTEXT"?
// In React, data normally flows from a parent component down to its children
// through "props". If many screens across the whole app need the same piece of
// data (like the logged-in user), passing it down by hand everywhere is painful.
// React Context lets us put a value in one place near the top of the app and let
// any component read it directly with a hook - here, `useAuth()`.
//
// WHAT THIS FILE PROVIDES
//   - `user`      : { username, role } when logged in, otherwise null
//   - `status`    : 'loading' | 'authed' | 'anon'  (used to guard routes)
//   - `pending2fa`: set while we're mid-login and the server asked for a 2FA code
//   - `login`, `verify2fa`, `register`, `logout` : the actions screens call
//
// It talks to the backend through `authService` (frontend/src/api/services/auth.ts)
// and remembers the session using the helpers in token.ts.

import {
    createContext,
    useContext,
    useEffect,
    useMemo,
    useState,
    type ReactNode,
} from 'react';
import { useNavigate } from 'react-router-dom';

import { authService } from '../api/services/auth';
import {
    getToken,
    setToken,
    setRefreshToken,
    setUsername,
    clearSession,
} from '../api/token';
import { decodeToken, isTokenExpired } from '../api/jwt';
import type { registerRequest } from '../types/dtos';

// The shape of the logged-in user we expose to the rest of the app.
interface AuthUser {
    username: string;
    role: string; // "STUDENT" | "PROFESSOR" | "ADMIN"
}

// Everything `useAuth()` gives back.
interface AuthContextValue {
    user: AuthUser | null;
    status: 'loading' | 'authed' | 'anon';
    pending2fa: { username: string } | null;
    // Returns whether the server now wants a 2FA code, plus the role if we're in.
    login: (
        username: string,
        password: string,
    ) => Promise<{ twoFactor: boolean; role?: string }>;
    verify2fa: (code: string) => Promise<{ role: string }>;
    register: (dto: registerRequest) => Promise<{ message: string; username: string }>;
    logout: () => Promise<void>;
    // Drop the session immediately, no backend call, no redirect. Used when a
    // login succeeded but we want to reject it (e.g. wrong role for this page).
    abandonSession: () => void;
}

// Create the context. The default value is only used if a component tries to call
// useAuth() without an <AuthProvider> above it - which would be a bug.
const AuthContext = createContext<AuthContextValue | undefined>(undefined);

// Helper: turn a raw token string into our small AuthUser object.
const userFromToken = (token: string): AuthUser | null => {
    const decoded = decodeToken(token);
    if (!decoded) return null;
    return { username: decoded.sub, role: decoded.role };
};

// <AuthProvider> wraps the whole app (see main.tsx). Everything inside it can
// call useAuth().
export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const navigate = useNavigate();

    const [user, setUser] = useState<AuthUser | null>(null);
    const [status, setStatus] = useState<'loading' | 'authed' | 'anon'>('loading');
    const [pending2fa, setPending2fa] = useState<{ username: string } | null>(null);

    // When the app first loads (or is refreshed) we check localStorage: is there
    // a token, and is it still valid? If so, restore the session; if not, clear
    // it and treat the visitor as logged out.
    useEffect(() => {
        const token = getToken();
        if (token && !isTokenExpired(token)) {
            const restored = userFromToken(token);
            if (restored) {
                setUser(restored);
                setStatus('authed');
                return;
            }
        }
        clearSession();
        setStatus('anon');
    }, []);

    // Step 1 of logging in: send username + password.
    const login = async (username: string, password: string) => {
        const res = await authService.login({ username, password });

        // The backend replies one of two ways:
        //  a) "2FA_REQUIRED" -> it emailed a 6-digit code, we must collect it next
        if (res.status === '2FA_REQUIRED') {
            setPending2fa({ username });
            return { twoFactor: true };
        }

        //  b) a token + refreshToken -> we're fully logged in
        if (res.token) {
            setToken(res.token);
            if (res.refreshToken) setRefreshToken(res.refreshToken);
            setUsername(username);

            const loggedIn = userFromToken(res.token);
            setUser(loggedIn);
            setStatus('authed');
            return { twoFactor: false, role: loggedIn?.role };
        }

        // Neither shape came back - shouldn't happen, but fail loudly.
        throw new Error(res.message ?? 'Login failed: unexpected server response');
    };

    // Step 2 (only if 2FA was required): send the code the user typed.
    const verify2fa = async (code: string) => {
        if (!pending2fa) {
            throw new Error('No login in progress');
        }
        // Note: this endpoint returns only a token, no refreshToken.
        const res = await authService.verify2fa(pending2fa.username, code);

        setToken(res.token);
        setUsername(pending2fa.username);

        const loggedIn = userFromToken(res.token);
        setUser(loggedIn);
        setStatus('authed');
        setPending2fa(null);
        return { role: loggedIn?.role ?? 'STUDENT' };
    };

    // Create a new account. This does NOT log the user in - they still need to
    // log in afterwards (that matches how the backend works).
    const register = (dto: registerRequest) => authService.register(dto);

    // Immediately forget the session locally. No backend call, no navigation -
    // the caller decides what to do next (usually show an error and stay put).
    const abandonSession = () => {
        clearSession();
        setUser(null);
        setStatus('anon');
        setPending2fa(null);
    };

    // Log out: tell the backend (best effort), wipe local storage, go home.
    const logout = async () => {
        try {
            if (user?.username) {
                await authService.logout(user.username);
            }
        } catch {
            // Even if the server call fails, we still clear the session locally.
        }
        clearSession();
        setUser(null);
        setStatus('anon');
        setPending2fa(null);
        navigate('/');
    };

    // useMemo keeps this object stable between renders unless something inside
    // actually changed - a small performance nicety for context.
    const value = useMemo<AuthContextValue>(
        () => ({ user, status, pending2fa, login, verify2fa, register, logout, abandonSession }),
        // eslint-disable-next-line react-hooks/exhaustive-deps
        [user, status, pending2fa],
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// The hook every screen uses:  const auth = useAuth();
export const useAuth = (): AuthContextValue => {
    const ctx = useContext(AuthContext);
    if (!ctx) {
        throw new Error('useAuth() must be used inside an <AuthProvider>');
    }
    return ctx;
};
