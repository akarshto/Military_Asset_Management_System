import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import ModuleShell from "../components/ModuleShell";

const localDateTime = () => {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    return now.toISOString().slice(0, 16);
};

const modules = {
    transfers: {
        title: "Transfers",
        description: "Move equipment between bases and review transfer records.",
        endpoint: "/transfers",
        dateField: "transferDate",
        textField: "remarks",
        textLabel: "Remarks",
        fields: [
            { name: "fromBaseId", label: "From base", type: "base" },
            { name: "toBaseId", label: "To base", type: "base" },
            { name: "equipmentTypeId", label: "Equipment type", type: "equipmentType" },
            { name: "quantity", label: "Quantity", type: "number" },
            { name: "transferDate", label: "Transfer date", type: "datetime-local" },
            { name: "remarks", label: "Remarks", type: "text" }
        ],
        columns: [
            { label: "ID", value: (record) => record.id },
            { label: "From", value: (record) => record.fromBase?.name },
            { label: "To", value: (record) => record.toBase?.name },
            { label: "Equipment type", value: (record) => record.equipmentType?.name },
            { label: "Quantity", value: (record) => record.quantity },
            { label: "Date", value: (record) => record.transferDate?.replace("T", " ") },
            { label: "Remarks", value: (record) => record.remarks }
        ]
    },
    assignments: {
        title: "Assignments",
        description: "Record equipment issued to personnel.",
        endpoint: "/assignments",
        dateField: "assignedDate",
        textField: "remarks",
        textLabel: "Remarks",
        fields: [
            { name: "baseId", label: "Base", type: "base" },
            { name: "equipmentTypeId", label: "Equipment type", type: "equipmentType" },
            { name: "personnelName", label: "Personnel name", type: "text" },
            { name: "quantity", label: "Quantity", type: "number" },
            { name: "assignedDate", label: "Assignment date", type: "datetime-local" },
            { name: "remarks", label: "Remarks", type: "text" }
        ],
        columns: [
            { label: "ID", value: (record) => record.id },
            { label: "Base", value: (record) => record.base?.name },
            { label: "Equipment type", value: (record) => record.equipmentType?.name },
            { label: "Personnel", value: (record) => record.personnelName },
            { label: "Quantity", value: (record) => record.quantity },
            { label: "Date", value: (record) => record.assignedDate?.replace("T", " ") },
            { label: "Remarks", value: (record) => record.remarks }
        ]
    },
    expenditures: {
        title: "Expenditures",
        description: "Record equipment expended from base inventory.",
        endpoint: "/expenditures",
        dateField: "expenditureDate",
        textField: "reason",
        textLabel: "Reason",
        fields: [
            { name: "baseId", label: "Base", type: "base" },
            { name: "equipmentTypeId", label: "Equipment type", type: "equipmentType" },
            { name: "quantity", label: "Quantity", type: "number" },
            { name: "expenditureDate", label: "Expenditure date", type: "datetime-local" },
            { name: "reason", label: "Reason", type: "text" }
        ],
        columns: [
            { label: "ID", value: (record) => record.id },
            { label: "Base", value: (record) => record.base?.name },
            { label: "Equipment type", value: (record) => record.equipmentType?.name },
            { label: "Quantity", value: (record) => record.quantity },
            { label: "Date", value: (record) => record.expenditureDate?.replace("T", " ") },
            { label: "Reason", value: (record) => record.reason }
        ]
    }
};

const emptyForm = (module, bases = [], equipmentTypes = []) => ({
    baseId: String(bases[0]?.id || ""),
    fromBaseId: String(bases[0]?.id || ""),
    toBaseId: String(bases[1]?.id || ""),
    equipmentTypeId: String(equipmentTypes[0]?.id || ""),
    quantity: "1",
    [modules[module].dateField]: localDateTime(),
    [modules[module].textField]: "",
    personnelName: ""
});

function OperationsPage({ module }) {
    const navigate = useNavigate();
    const config = modules[module];
    const [records, setRecords] = useState([]);
    const [bases, setBases] = useState([]);
    const [equipmentTypes, setEquipmentTypes] = useState([]);
    const [form, setForm] = useState(() => emptyForm(module));
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [showForm, setShowForm] = useState(false);

    useEffect(() => {
        if (!localStorage.getItem("token")) {
            navigate("/login");
            return;
        }

        const loadData = async () => {
            setLoading(true);
            setError("");
            try {
                const [recordsResponse, basesResponse, typesResponse] = await Promise.all([
                    api.get(config.endpoint),
                    api.get("/bases"),
                    api.get("/equipment/types")
                ]);
                setRecords(recordsResponse.data);
                setBases(basesResponse.data);
                setEquipmentTypes(typesResponse.data);
                setForm(emptyForm(module, basesResponse.data, typesResponse.data));
            } catch (err) {
                if (err.response?.status === 401 || err.response?.status === 403) {
                    localStorage.removeItem("token");
                    localStorage.removeItem("username");
                    navigate("/login");
                } else {
                    setError(`Unable to load ${config.title.toLowerCase()}.`);
                }
            } finally {
                setLoading(false);
            }
        };

        loadData();
    }, [config.endpoint, config.title, module, navigate]);

    const updateForm = (event) => {
        const { name, value } = event.target;
        setForm((currentForm) => ({ ...currentForm, [name]: value }));
    };

    const submit = async (event) => {
        event.preventDefault();
        setSaving(true);
        setError("");
        const commonFields = {
            equipmentType: { id: Number(form.equipmentTypeId) },
            quantity: Number(form.quantity),
            [config.dateField]: form[config.dateField],
            [config.textField]: form[config.textField]
        };
        const payload = module === "transfers"
            ? {
                ...commonFields,
                fromBase: { id: Number(form.fromBaseId) },
                toBase: { id: Number(form.toBaseId) }
            }
            : {
                ...commonFields,
                base: { id: Number(form.baseId) },
                ...(module === "assignments" ? { personnelName: form.personnelName } : {})
            };

        try {
            const response = await api.post(config.endpoint, payload);
            setRecords((currentRecords) => [...currentRecords, response.data]);
            setForm(emptyForm(module, bases, equipmentTypes));
            setShowForm(false);
        } catch (err) {
            setError(err.response?.data?.message || `Unable to save ${config.title.toLowerCase()}.`);
        } finally {
            setSaving(false);
        }
    };

    const optionsFor = (field) => {
        if (field.type === "equipmentType") {
            return equipmentTypes;
        }
        if (field.name === "fromBaseId") {
            return bases.filter((base) => String(base.id) !== form.toBaseId);
        }
        if (field.name === "toBaseId") {
            return bases.filter((base) => String(base.id) !== form.fromBaseId);
        }
        return bases;
    };

    const formReady = module === "transfers"
        ? bases.length >= 2 && equipmentTypes.length > 0
        : bases.length > 0 && equipmentTypes.length > 0;

    return (
        <ModuleShell activePage={config.title}>
            <div className="page-header">
                <div>
                    <h1>{config.title}</h1>
                    <p>{config.description}</p>
                </div>
                <button className="primary-button" onClick={() => setShowForm((visible) => !visible)}>
                    + Add {module === "expenditures" ? "Expenditure" : module.slice(0, -1)}
                </button>
            </div>

            {showForm && (
                <form className="form-panel" onSubmit={submit}>
                    {config.fields.map((field) => (
                        <label key={field.name}>
                            {field.label}
                            {field.type === "base" || field.type === "equipmentType" ? (
                                <select
                                    name={field.name}
                                    value={form[field.name]}
                                    onChange={updateForm}
                                    required
                                >
                                    <option value="" disabled>Select {field.label.toLowerCase()}</option>
                                    {optionsFor(field).map((option) => (
                                        <option key={option.id} value={option.id}>
                                            {field.type === "base" ? option.name : option.name}
                                        </option>
                                    ))}
                                </select>
                            ) : (
                                <input
                                    name={field.name}
                                    type={field.type}
                                    min={field.type === "number" ? 1 : undefined}
                                    required={field.name !== config.textField}
                                    value={form[field.name]}
                                    onChange={updateForm}
                                />
                            )}
                        </label>
                    ))}
                    <div className="form-actions">
                        <button className="primary-button" type="submit" disabled={saving || !formReady}>
                            {saving ? "Saving..." : `Save ${module === "expenditures" ? "Expenditure" : module.slice(0, -1)}`}
                        </button>
                        <button className="secondary-button" type="button" onClick={() => setShowForm(false)}>
                            Cancel
                        </button>
                    </div>
                    {!formReady && (
                        <p className="form-hint">
                            {module === "transfers"
                                ? "Transfers require at least two bases and one equipment type."
                                : "Add a base and equipment type before recording this item."}
                        </p>
                    )}
                </form>
            )}

            {error && <div className="error-message">{error}</div>}
            {loading ? (
                <div className="loading">Loading {config.title.toLowerCase()}...</div>
            ) : (
                <div className="table-card">
                    <table className="data-table">
                        <thead>
                            <tr>{config.columns.map((column) => <th key={column.label}>{column.label}</th>)}</tr>
                        </thead>
                        <tbody>
                            {records.length === 0 ? (
                                <tr>
                                    <td colSpan={config.columns.length} className="empty-state">
                                        No {config.title.toLowerCase()} found.
                                    </td>
                                </tr>
                            ) : records.map((record) => (
                                <tr key={record.id}>
                                    {config.columns.map((column) => (
                                        <td key={column.label}>{column.value(record) || "-"}</td>
                                    ))}
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </ModuleShell>
    );
}

export default OperationsPage;