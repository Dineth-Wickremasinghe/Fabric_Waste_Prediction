-- Run this once to fix cutting_jobs constraints
ALTER TABLE cutting_jobs ALTER COLUMN job_id DROP NOT NULL;
ALTER TABLE cutting_jobs ALTER COLUMN material_id DROP NOT NULL;
ALTER TABLE cutting_jobs ALTER COLUMN operator_id DROP NOT NULL;
ALTER TABLE cutting_jobs ALTER COLUMN status DROP NOT NULL;
ALTER TABLE cutting_jobs ALTER COLUMN actual_wastage_pct DROP NOT NULL;
ALTER TABLE cutting_jobs ALTER COLUMN cutting_method DROP NOT NULL;
ALTER TABLE cutting_jobs ALTER COLUMN cutting_overlap_mm DROP NOT NULL;
ALTER TABLE cutting_jobs ALTER COLUMN marker_efficiency_pct DROP NOT NULL;
ALTER TABLE cutting_jobs ALTER COLUMN predicted_waste_pct DROP NOT NULL;
ALTER TABLE cutting_jobs ALTER COLUMN shift DROP NOT NULL;