CREATE EXTENSION IF NOT EXISTS dblink;

-- Crea la base solo si no existe
DO
$$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_database WHERE datname = 'tpi_backend_db'
   ) THEN
      PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE tpi_backend_db');
   END IF;
END
$$;
