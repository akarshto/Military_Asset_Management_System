import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Equipment from "./pages/Equipment";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Bases from "./pages/Bases";
import Purchases from "./pages/Purchases";
import OperationsPage from "./pages/OperationsPage";
import AuditLogs from "./pages/AuditLogs";

function App() {
    return (
        <BrowserRouter>

            <Routes>

                <Route path="/login" element={<Login />} />

                <Route path="/dashboard" element={<Dashboard />} />

                <Route path="/bases" element={<Bases />} />
		
		<Route path="/equipment" element={<Equipment />} />

                <Route path="/purchases" element={<Purchases />} />

                <Route path="/transfers" element={<OperationsPage module="transfers" />} />

                <Route path="/assignments" element={<OperationsPage module="assignments" />} />

                <Route path="/expenditures" element={<OperationsPage module="expenditures" />} />

                <Route path="/audit-logs" element={<AuditLogs />} />

                <Route
                    path="/"
                    element={<Navigate to="/login" replace />}
                />

            </Routes>

        </BrowserRouter>
    );
}

export default App;
