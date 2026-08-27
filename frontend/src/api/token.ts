// Minimal JWT store backed by localStorage. Dependency-free so client.ts can
// import it without an import cycle. The full auth context / login UI is a
// separate piece of work.

const TOKEN_KEY = 'vsu_token';

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
