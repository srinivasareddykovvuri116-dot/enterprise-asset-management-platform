import React from "react";
import ReactDOM from "react-dom/client";
import { Provider } from "react-redux";

import App from "./App";
import { store } from "./store/store";
import { logout } from "./store/authSlice";

import "./index.css";

window.addEventListener("auth:logout", () => {
  store.dispatch(logout());
});

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <Provider store={store}>
      <App />
    </Provider>
  </React.StrictMode>
);