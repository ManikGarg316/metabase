(ns metabase-enterprise.metabot-v3.tools.transforms-test
  (:require
   [clojure.test :refer :all]
   [metabase-enterprise.metabot-v3.tools.transforms :as metabot-v3.tools.transforms]
   [metabase.api.common :as api]
   [metabase.permissions.models.permissions-group :as perms-group]
   [metabase.test :as mt]))

(deftest get-transforms-test
  (testing "get-transforms returns transforms with expected fields"
    (mt/with-premium-features #{:metabot-v3 :transforms}
      (mt/with-temp [:model/Transform transform
                     {:name   "Test Transform"
                      :source {:type  "query"
                               :query {:database (mt/id)
                                       :type     "native"
                                       :native   {:query "SELECT 1"}}}}]
        (mt/with-data-analyst-role! (mt/user->id :rasta)
          (mt/with-db-perm-for-group! (perms-group/all-users) (mt/id) :perms/transforms :yes
            (mt/with-db-perm-for-group! (perms-group/all-users) (mt/id) :perms/view-data :unrestricted
              (binding [api/*current-user-id*  (mt/user->id :rasta)
                        api/*is-superuser?*    false
                        api/*is-data-analyst?* true]
                (let [result   (:structured_output (metabot-v3.tools.transforms/get-transforms {}))
                      returned (first (filter #(= (:id transform) (:id %)) result))]
                  (testing "returns the transform"
                    (is (some? returned)))
                  (testing "includes expected fields"
                    (is (= (:id transform) (:id returned)))
                    (is (= "Test Transform" (:name returned)))
                    (is (some? (:source returned)))))))))))))

(deftest get-transforms-filters-unreadable-test
  (testing "get-transforms filters out transforms the user cannot read"
    (mt/with-premium-features #{:metabot-v3 :transforms}
      (mt/with-temp [:model/Database {db-id :id} {}
                     :model/Transform transform
                     {:name   "Blocked Transform"
                      :source {:type  "query"
                               :query {:database db-id
                                       :type     "native"
                                       :native   {:query "SELECT 1"}}}}]
        (testing "when user has access, transform is returned"
          (mt/with-data-analyst-role! (mt/user->id :rasta)
            (mt/with-db-perm-for-group! (perms-group/all-users) db-id :perms/transforms :yes
              (mt/with-db-perm-for-group! (perms-group/all-users) db-id :perms/view-data :unrestricted
                (binding [api/*current-user-id*  (mt/user->id :rasta)
                          api/*is-superuser?*    false
                          api/*is-data-analyst?* true]
                  (let [result   (:structured_output (metabot-v3.tools.transforms/get-transforms {}))
                        returned (first (filter #(= (:id transform) (:id %)) result))]
                    (is (some? returned))))))))

        (testing "when user has blocked access, transform is filtered out"
          (mt/with-user-in-groups [group {:name "Blocked Group"}
                                   user [group]]
            (mt/with-data-analyst-role! (:id user)
              (mt/with-db-perm-for-group! (perms-group/all-users) db-id :perms/transforms :yes
                (mt/with-db-perm-for-group! group db-id :perms/transforms :yes
                  (mt/with-db-perm-for-group! (perms-group/all-users) db-id :perms/view-data :blocked
                    (mt/with-db-perm-for-group! group db-id :perms/view-data :blocked
                      (binding [api/*current-user-id*  (:id user)
                                api/*is-superuser?*    false
                                api/*is-data-analyst?* true]
                        (let [result   (:structured_output (metabot-v3.tools.transforms/get-transforms {}))
                              returned (first (filter #(= (:id transform) (:id %)) result))]
                          (is (nil? returned)))))))))))))))

(deftest get-transform-details-test
  (testing "get-transform-details returns transform with expected fields"
    (mt/with-premium-features #{:metabot-v3 :transforms}
      (mt/with-temp [:model/Transform transform
                     {:name   "Test Transform"
                      :source {:type  "query"
                               :query {:database (mt/id)
                                       :type     "native"
                                       :native   {:query "SELECT 1"}}}}]
        (mt/with-data-analyst-role! (mt/user->id :rasta)
          (mt/with-db-perm-for-group! (perms-group/all-users) (mt/id) :perms/transforms :yes
            (mt/with-db-perm-for-group! (perms-group/all-users) (mt/id) :perms/view-data :unrestricted
              (binding [api/*current-user-id*  (mt/user->id :rasta)
                        api/*is-superuser?*    false
                        api/*is-data-analyst?* true]
                (let [result (:structured_output (metabot-v3.tools.transforms/get-transform-details
                                                  {:transform-id (:id transform)}))]
                  (testing "returns the transform with all fields"
                    (is (= (:id transform) (:id result)))
                    (is (= "Test Transform" (:name result)))
                    (is (some? (:source result)))))))))))))

(deftest get-transform-details-blocked-test
  (testing "get-transform-details throws when user cannot read transform"
    (mt/with-premium-features #{:metabot-v3 :transforms}
      (mt/with-temp [:model/Database {db-id :id} {}
                     :model/Transform transform
                     {:name   "Blocked Transform"
                      :source {:type  "query"
                               :query {:database db-id
                                       :type     "native"
                                       :native   {:query "SELECT 1"}}}}]
        (mt/with-user-in-groups [group {:name "Blocked Group"}
                                 user [group]]
          (mt/with-data-analyst-role! (:id user)
            (mt/with-db-perm-for-group! (perms-group/all-users) db-id :perms/transforms :yes
              (mt/with-db-perm-for-group! group db-id :perms/transforms :yes
                (mt/with-db-perm-for-group! (perms-group/all-users) db-id :perms/view-data :blocked
                  (mt/with-db-perm-for-group! group db-id :perms/view-data :blocked
                    (binding [api/*current-user-id*  (:id user)
                              api/*is-superuser?*    false
                              api/*is-data-analyst?* true]
                      (is (thrown-with-msg?
                           clojure.lang.ExceptionInfo
                           #"You don't have permissions to do that"
                           (metabot-v3.tools.transforms/get-transform-details
                            {:transform-id (:id transform)}))))))))))))))
