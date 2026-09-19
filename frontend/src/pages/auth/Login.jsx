import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useDispatch } from "react-redux";

import {
  Mail,
  LockKeyhole,
  AlertCircle,
  Loader2,
  ShieldCheck,
  LogIn,
} from "lucide-react";

import { loginUser } from "../../api/authApi";
import { setCredentials } from "../../store/authSlice";

function Login() {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    event.stopPropagation();

    setError("");
    setLoading(true);

    try {
      const response = await loginUser(formData);

      console.log("Login response:", response);

      const user = {
        userId: response.userId,
        organizationId: response.organizationId,
        role: response.role,
      };

      dispatch(
        setCredentials({
          token: response.token,
          user,
        })
      );

      navigate("/dashboard", { replace: true });
    } catch (err) {
      console.error("Login error:", err);

      const message =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Login failed. Please check your credentials.";

      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100">
      <div className="flex min-h-screen items-center justify-center px-4 py-10">
        <div className="w-full max-w-lg">

          {/* Header */}
          <div className="mb-8 text-center">
            <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-slate-950 text-white shadow-sm">
              <ShieldCheck size={26} />
            </div>

            <h1 className="text-2xl font-bold text-slate-950">
              Asset Management
            </h1>

            <p className="mt-1 text-sm text-slate-500">
              Enterprise Platform
            </p>
          </div>

          {/* Login Card */}
          <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">

            <div className="border-b border-slate-200 px-6 py-6 sm:px-8">
              <h2 className="text-xl font-semibold text-slate-900">
                Welcome back
              </h2>

              <p className="mt-1.5 text-sm text-slate-500">
                Sign in to access your enterprise workspace.
              </p>
            </div>

            <form
              onSubmit={handleSubmit}
              className="space-y-5 px-6 py-6 sm:px-8"
            >
              {/* Error */}
              {error && (
                <div className="flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                  <AlertCircle
                    size={18}
                    className="mt-0.5 shrink-0"
                  />

                  <span>{error}</span>
                </div>
              )}

              {/* Email */}
              <div>
                <label
                  htmlFor="email"
                  className="mb-2 block text-sm font-medium text-slate-700"
                >
                  Email
                </label>

                <div className="relative">
                  <Mail
                    size={17}
                    className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                  />

                  <input
                    id="email"
                    name="email"
                    type="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="Enter your email"
                    autoComplete="email"
                    required
                    disabled={loading}
                    className="w-full rounded-lg border border-slate-300 bg-white py-2.5 pl-10 pr-3 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200 disabled:bg-slate-100"
                  />
                </div>
              </div>

              {/* Password */}
              <div>
                <label
                  htmlFor="password"
                  className="mb-2 block text-sm font-medium text-slate-700"
                >
                  Password
                </label>

                <div className="relative">
                  <LockKeyhole
                    size={17}
                    className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                  />

                  <input
                    id="password"
                    name="password"
                    type="password"
                    value={formData.password}
                    onChange={handleChange}
                    placeholder="Enter your password"
                    autoComplete="current-password"
                    required
                    disabled={loading}
                    className="w-full rounded-lg border border-slate-300 bg-white py-2.5 pl-10 pr-3 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200 disabled:bg-slate-100"
                  />
                </div>
              </div>

              {/* Submit */}
              <button
                type="submit"
                disabled={loading}
                className="flex w-full items-center justify-center gap-2 rounded-lg bg-slate-950 px-4 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {loading ? (
                  <>
                    <Loader2
                      size={17}
                      className="animate-spin"
                    />
                    Signing in...
                  </>
                ) : (
                  <>
                    <LogIn size={17} />
                    Sign In
                  </>
                )}
              </button>
            </form>

            {/* Register */}
            <div className="border-t border-slate-200 bg-slate-50 px-6 py-5 text-center sm:px-8">
              <p className="text-sm text-slate-500">
                Don't have an account?{" "}

                <Link
                  to="/register"
                  className="font-semibold text-slate-900 hover:underline"
                >
                  Create organization
                </Link>
              </p>
            </div>
          </div>

          <p className="mt-6 text-center text-xs text-slate-400">
            Secure enterprise workspace
          </p>
        </div>
      </div>
    </div>
  );
}

export default Login;