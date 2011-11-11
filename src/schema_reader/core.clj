(ns schema-reader.core
  (:require [clojure.java.jdbc :as sql]))

; define the database connection
(def db {:subprotocol "postgresql" :subname "//localhost:5432/schema_test" :username "rontoland" :password ""})

(defn sql-query [q]
  (sql/with-query-results res q (doall res)
  )
)

(defn get-table-schema [table]
  (seq
    (sql/with-connection db
      (sql-query
        [(str "SELECT attname
               FROM pg_attribute, pg_type
               WHERE typname = '" table "' AND attrelid = typrelid
               AND attname NOT IN ('cmin', 'cmax', 'xmin', 'xmax', 'ctid', 'tableoid');"
        )]
      )
    )
  )
)
