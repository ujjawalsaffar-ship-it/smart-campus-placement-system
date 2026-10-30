import streamlit as st
import pandas as pd
import database

st.set_page_config(
    page_title="Smart Campus Placement Management System",
    page_icon="🎓",
    layout="wide"
)

# Custom Styling
st.markdown("""
<style>
    .main-title {
        font-size: 2.2rem;
        font-weight: 700;
        color: #1E3A8A;
        margin-bottom: 0.2rem;
    }
    .sub-title {
        color: #4B5563;
        font-size: 1.05rem;
        margin-bottom: 1.5rem;
    }
    .metric-card {
        background-color: #F3F4F6;
        border-radius: 8px;
        padding: 16px;
        border-left: 4px solid #3B82F6;
    }
</style>
""", unsafe_allow_html=True)

# Ensure database is initialized
database.init_db(force=False)

st.markdown('<div class="main-title">🎓 Smart Campus Placement Management System</div>', unsafe_allow_html=True)
st.markdown('<div class="sub-title">RDBMS Database Engine with Java Backend & Web Dashboard</div>', unsafe_allow_html=True)

# Navigation tabs
tab_dash, tab_student, tab_jobs, tab_admin = st.tabs([
    "📊 TPO Analytics Dashboard",
    "👨‍🎓 Student Portal (Apply)",
    "🏢 Companies & Drives",
    "⚙️ Application Management (TPO Admin)"
])

# -----------------------------------------------------------------------------
# TAB 1: TPO Analytics Dashboard
# -----------------------------------------------------------------------------
with tab_dash:
    st.subheader("Placement Performance Overview")
    
    summary_data = database.get_placement_summary()
    df_summary = pd.DataFrame(summary_data)
    
    students = database.get_students()
    df_students = pd.DataFrame(students)
    
    total_students = len(df_students)
    placed_students = len(df_students[df_students['placement_status'] == 'Placed']) if not df_students.empty else 0
    overall_pct = round((placed_students / total_students) * 100, 1) if total_students > 0 else 0.0
    
    col1, col2, col3, col4 = st.columns(4)
    col1.metric("Total Registered Students", total_students)
    col2.metric("Total Placed", placed_students)
    col3.metric("Overall Placement Rate", f"{overall_pct}%")
    
    # Calculate avg package of placed students
    apps = database.get_applications()
    df_apps = pd.DataFrame(apps)
    placed_apps = df_apps[df_apps['status'] == 'Selected'] if not df_apps.empty else pd.DataFrame()
    avg_pkg = round(placed_apps['package_lpa'].mean(), 2) if not placed_apps.empty else 0.0
    col4.metric("Avg Placed Package", f"{avg_pkg} LPA")
    
    st.markdown("---")
    st.subheader("Department-wise Statistics (View: `v_placement_summary`)")
    
    if not df_summary.empty:
        col_table, col_chart = st.columns([1.2, 1])
        with col_table:
            st.dataframe(
                df_summary.rename(columns={
                    "department": "Department",
                    "total_students": "Total Students",
                    "placed_students": "Placed",
                    "placement_percentage": "Placement %",
                    "avg_placed_package_lpa": "Avg Package (LPA)",
                    "highest_package_lpa": "Highest Package (LPA)"
                }),
                use_container_width=True,
                hide_index=True
            )
        with col_chart:
            st.bar_chart(df_summary.set_index("department")["placement_percentage"])
    else:
        st.info("No placement data available.")

# -----------------------------------------------------------------------------
# TAB 2: Student Portal
# -----------------------------------------------------------------------------
with tab_student:
    st.subheader("Student Application Portal")
    
    if df_students.empty:
        st.warning("No students found.")
    else:
        # Select Student
        student_options = {f"{s['student_id']}: {s['name']} ({s['department']} | CGPA: {s['cgpa']})": s['student_id'] for s in students}
        selected_label = st.selectbox("Select Student Profile:", list(student_options.keys()))
        selected_id = student_options[selected_label]
        
        current_student = next(s for s in students if s['student_id'] == selected_id)
        
        st.info(f"**Selected Student:** {current_student['name']} | **Dept:** {current_student['department']} | **CGPA:** {current_student['cgpa']} | **Placement Status:** `{current_student['placement_status']}`")
        
        # Eligible jobs from View v_student_eligible_jobs
        st.markdown("#### Eligible Job Drives for You")
        eligible_jobs = database.get_eligible_jobs_for_student(selected_id)
        
        if eligible_jobs:
            df_eligible = pd.DataFrame(eligible_jobs)
            st.dataframe(
                df_eligible.rename(columns={
                    "company_name": "Company",
                    "role_title": "Role",
                    "min_cgpa": "Min CGPA",
                    "package_lpa": "Package (LPA)",
                    "deadline": "Deadline",
                    "my_status": "My Application Status"
                }),
                use_container_width=True,
                hide_index=True
            )
        else:
            st.warning("No active jobs meet your current CGPA criteria.")
            
        st.markdown("---")
        st.markdown("#### Apply for a Job Opening")
        st.caption("🧪 **Trigger Demonstration:** If you attempt to apply for a job where your CGPA is below the minimum criteria, Database Trigger 1 will block the transaction.")
        
        all_jobs = database.get_job_postings()
        job_options = {f"[Job #{j['job_id']}] {j['company_name']} - {j['role_title']} (Min CGPA: {j['min_cgpa']} | {j['package_lpa']} LPA)": j['job_id'] for j in all_jobs}
        
        col_apply_job, col_apply_btn = st.columns([3, 1])
        with col_apply_job:
            chosen_job_label = st.selectbox("Select Job to Apply:", list(job_options.keys()))
            chosen_job_id = job_options[chosen_job_label]
        with col_apply_btn:
            st.write("")
            st.write("")
            if st.button("Submit Application", type="primary"):
                success, msg = database.apply_for_job(selected_id, chosen_job_id)
                if success:
                    st.success(f"✅ {msg}")
                    st.rerun()
                else:
                    st.error(f"❌ {msg}")

# -----------------------------------------------------------------------------
# TAB 3: Companies & Drives
# -----------------------------------------------------------------------------
with tab_jobs:
    st.subheader("Active Placement Drives & Recruiting Companies")
    
    jobs = database.get_job_postings()
    if jobs:
        df_jobs = pd.DataFrame(jobs)
        st.dataframe(
            df_jobs.rename(columns={
                "job_id": "Job ID",
                "company_name": "Company",
                "role_title": "Job Role",
                "min_cgpa": "Min CGPA Required",
                "package_lpa": "Package (LPA)",
                "deadline": "Deadline",
                "total_applications": "Total Applicants"
            }),
            use_container_width=True,
            hide_index=True
        )
    
    st.markdown("---")
    st.subheader("Post a New Job Opening")
    companies = database.get_companies()
    if companies:
        comp_opts = {c['company_name']: c['company_id'] for c in companies}
        with st.form("new_job_form"):
            c_name = st.selectbox("Company", list(comp_opts.keys()))
            c_id = comp_opts[c_name]
            role = st.text_input("Role Title (e.g. Cloud Engineer)")
            min_cgpa = st.number_input("Minimum CGPA", min_value=0.0, max_value=10.0, value=7.5, step=0.1)
            package = st.number_input("Package in LPA", min_value=1.0, max_value=100.0, value=10.0, step=0.5)
            deadline = st.date_input("Deadline")
            
            submitted = st.form_submit_button("Post Opening")
            if submitted:
                if role.strip():
                    ok, msg = database.add_job_posting(c_id, role, min_cgpa, package, str(deadline))
                    if ok:
                        st.success(msg)
                        st.rerun()
                    else:
                        st.error(msg)
                else:
                    st.error("Please provide a role title.")

# -----------------------------------------------------------------------------
# TAB 4: Application Management (TPO Admin)
# -----------------------------------------------------------------------------
with tab_admin:
    st.subheader("All Student Applications")
    st.caption("🧪 **Trigger Demonstration:** Changing an application status to 'Selected' will cause Trigger 2 to automatically synchronize the student's status to 'Placed'.")
    
    all_apps = database.get_applications()
    if all_apps:
        df_all_apps = pd.DataFrame(all_apps)
        st.dataframe(
            df_all_apps[[
                "application_id", "student_name", "department", "cgpa",
                "company_name", "role_title", "package_lpa", "status", "application_date"
            ]].rename(columns={
                "application_id": "App ID",
                "student_name": "Student",
                "department": "Dept",
                "cgpa": "CGPA",
                "company_name": "Company",
                "role_title": "Role",
                "package_lpa": "LPA",
                "status": "Status",
                "application_date": "Applied On"
            }),
            use_container_width=True,
            hide_index=True
        )
        
        st.markdown("---")
        st.subheader("Update Application Status")
        col_app_sel, col_stat_sel, col_update_btn = st.columns([2, 1.5, 1])
        with col_app_sel:
            app_choices = {
                f"App #{a['application_id']}: {a['student_name']} -> {a['company_name']} ({a['role_title']}) [Current: {a['status']}]": a['application_id']
                for a in all_apps
            }
            selected_app_label = st.selectbox("Select Application:", list(app_choices.keys()))
            selected_app_id = app_choices[selected_app_label]
        with col_stat_sel:
            new_status = st.selectbox("New Status:", ["Shortlisted", "Selected", "Rejected"])
        with col_update_btn:
            st.write("")
            st.write("")
            if st.button("Update Status", type="primary"):
                ok, msg = database.update_application_status(selected_app_id, new_status)
                if ok:
                    st.success(msg)
                    st.rerun()
                else:
                    st.error(msg)
    else:
        st.info("No applications submitted yet.")
