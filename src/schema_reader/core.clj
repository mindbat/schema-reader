(ns schema-reader.core
  (:require [clojure.java.jdbc :as sql])
)

;; define the database connection
(def db 
  {:subprotocol "postgresql" 
   :subname "//localhost:5432/schema_test"
   :username "schema_reader"
   :password ""
  }
)

(defn sql-query [q]
  (seq (sql/with-connection db (sql/with-query-results res q (doall res))))
)

(defn get-table-list []
  (map :tablename (sql-query
    [(str "SELECT tablename FROM pg_tables WHERE tablename NOT LIKE 'pg%' AND tablename NOT LIKE 'sql%';")])
  )
)

(defn get-table-schema [table]
  (sql-query
    [(str "SELECT attname
           FROM pg_attribute, pg_type
           WHERE typname = '" table "' AND attrelid = typrelid
           AND attname NOT IN ('cmin', 'cmax', 'xmin', 'xmax', 'ctid', 'tableoid');"
    )]
  )
)

;; get a sequence of maps describing the db tables
(defn get-db-schema []
  (for [table (get-table-list)]
    {:tablename table :columns (vec (map :attname (get-table-schema table)))}
  )
)
