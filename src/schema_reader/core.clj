(ns schema-reader.core
  (:require [clojure.java.jdbc :as sql])
)

;; db connection for debugging
(def *db* 
  {:subprotocol "postgresql" 
   :subname "//localhost:5432/schema_test"
   :username "schema_reader"
   :password ""
  }
)

(defn sql-query 
  "Basic function for querying the database"
  [db q]
  (seq (sql/with-connection db (sql/with-query-results res q (doall res))))
)

;; get list of tables for the given database
(defmulti get-table-list :subprotocol)

(defmethod get-table-list "postgresql" [db]
  (map :tablename (sql-query
    db
    [(str "SELECT tablename FROM pg_tables WHERE tablename NOT LIKE 'pg%' AND tablename NOT LIKE 'sql%';")])
  )
)

(defmethod get-table-list "mysql" [db]
  (seq 1 2 3 4 5)
)

;; get list of columns for the given table
(defmulti get-table-schema :subprotocol)

(defmethod get-table-schema "postgresql" [db table]
  (sql-query
    db
    [(str "SELECT attname
           FROM pg_attribute, pg_type
           WHERE typname = '" table "' AND attrelid = typrelid
           AND attname NOT IN ('cmin', 'cmax', 'xmin', 'xmax', 'ctid', 'tableoid');"
    )]
  )
)

(defmethod get-table-schema "mysql" [db table]
  {:attname "not-really-supported"}
)

(defn get-db-schema 
  "Get a sequence of maps describing all the tables in the given database"
  [db]
  (for [table (get-table-list db)]
    {:tablename table :columns (vec (map :attname (get-table-schema db table)))}
  )
)
