import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import {
  UserRound,
  Mail,
  LockKeyhole,
  Building2,
  UserPlus,
  AlertCircle,
  CheckCircle2,
  Loader2,
  ShieldCheck,
} from "lucide-react";

import { registerUser } from "../../api/authApi";

function Register() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    email: "",
    password: "",
    fullName: "",
    organizationName: "",
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
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

    setError("");
    setSuccess("");
    setLoading(true);

    try {
      await registerUser(formData);

      setSuccess(
        "Registration successful. Redirecting to login..."
      );

      setTimeout(() => {
        navigate("/login");
      }, 1000);
    } catch (err) {
      const message =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Registration failed. Please try again.";

      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100">

      {/* ==========================================
          MAIN
      =========================================== */}

      <div className="flex min-h-screen items-center justify-center px-4 py-10">

        <div className="w-full max-w-lg">

          {/* Branding */}

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

          {/* Registration Card */}

          <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">

            {/* Header */}

            <div className="border-b border-slate-200 px-6 py-6 sm:px-8">

              <h2 className="text-xl font-semibold text-slate-900">
                Create your organization
              </h2>

              <p className="mt-1.5 text-sm text-slate-500">
                Set up your workspace and administrator account.
              </p>

            </div>

            {/* Form */}

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

              {/* Success */}

              {success && (
                <div className="flex items-start gap-3 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">

                  <CheckCircle2
                    size={18}
                    className="mt-0.5 shrink-0"
                  />

                  <span>{success}</span>

                </div>
              )}

              {/* Full Name */}

              <div>

                <label
                  htmlFor="fullName"
                  className="mb-2 block text-sm font-medium text-slate-700"
                >
                  Full Name
                </label>

                <div className="relative">

                  <UserRound
                    size={17}
                    className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                  />

                  <input
                    id="fullName"
                    name="fullName"
                    type="text"
                    value={formData.fullName}
                    onChange={handleChange}
                    placeholder="Enter your full name"
                    autoComplete="name"
                    required
                    className="w-full rounded-lg border border-slate-300 bg-white py-2.5 pl-10 pr-3 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
                  />

                </div>

              </div>

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
                    className="w-full rounded-lg border border-slate-300 bg-white py-2.5 pl-10 pr-3 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
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
                    placeholder="Minimum 8 characters"
                    autoComplete="new-password"
                    minLength={8}
                    required
                    className="w-full rounded-lg border border-slate-300 bg-white py-2.5 pl-10 pr-3 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
                  />

                </div>

                <p className="mt-1.5 text-xs text-slate-400">
                  Password must contain at least 8 characters.
                </p>

              </div>

              {/* Organization */}

              <div>

                <label
                  htmlFor="organizationName"
                  className="mb-2 block text-sm font-medium text-slate-700"
                >
                  Organization Name
                </label>

                <div className="relative">

                  <Building2
                    size={17}
                    className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                  />

                  <input
                    id="organizationName"
                    name="organizationName"
                    type="text"
                    value={formData.organizationName}
                    onChange={handleChange}
                    placeholder="Enter organization name"
                    autoComplete="organization"
                    required
                    className="w-full rounded-lg border border-slate-300 bg-white py-2.5 pl-10 pr-3 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
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
                    Creating...
                  </>
                ) : (
                  <>
                    <UserPlus size={17} />
                    Create Organization
                  </>
                )}

              </button>

            </form>

            {/* Footer */}

            <div className="border-t border-slate-200 bg-slate-50 px-6 py-5 text-center sm:px-8">

              <p className="text-sm text-slate-500">
                Already have an account?{" "}
                <Link
                  to="/login"
                  className="font-semibold text-slate-900 hover:underline"
                >
                  Sign in
                </Link>
              </p>

            </div>

          </div>

          {/* Footer */}

          <p className="mt-6 text-center text-xs text-slate-400">
            Secure enterprise workspace
          </p>

        </div>

      </div>

    </div>
  );
}

export default Register;