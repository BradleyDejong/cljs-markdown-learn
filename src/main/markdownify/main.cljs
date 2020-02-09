(ns markdownify.main
  (:require [reagent.core :as reagent]
            ["showdown" :as showdown]
            ["copy-text-to-clipboard" :as copy]))

(defonce text-state (reagent/atom {:format :md
                                   :value ""}))

(defonce showdown-converter (showdown/Converter.))

(defonce flash-message (reagent/atom nil))
(defonce flash-timeout (reagent/atom nil))

(defn flash
  ([msg]
   (flash msg 3000))
  ([msg timeout]
   (js/clearTimeout @flash-timeout)
   (reset! flash-message msg)
   (reset! flash-timeout (js/setTimeout #(reset! flash-message nil) timeout))))

(defn md->html [md]
  (.makeHtml showdown-converter md))

(defn html->md [html]
  (.makeMarkdown showdown-converter html))

(defn ->md [{:keys [format value]}]
  (case format
    :md value
    :html (html->md value)))

(defn ->html [{:keys [format value]}]
  (case format
    :md (md->html value)
    :html value))

(defn app []
  [:div
   [:h1 "Markdownify"]
   [:div
    {:style {:position :absolute
             :top 0
             :left 0
             :right 0
             :margin :auto
             :max-width "400px"
             :background-color :red
             :text-align :center
             :padding "1em"
             :z-index 100
             :border-bottom-left-radius 10
             :border-bottom-right-radius 10
             :transition "transform 300ms ease-in-out"
             :transform (if @flash-message
                          "scaleY(1)"
                          "scaleY(0)")}}
    @flash-message]
   [:div {:style {:display :flex :align-items :stretch} }
    [:div {:style {:margin-right "1em" :flex "1" :display :flex :flex-direction :column}}
     [:h2 "Markdown"]
     [:textarea
      {:on-change (fn [x]
                    (reset! text-state {:format :md :value (-> x .-target .-value)}))
       :value (->md @text-state)
       :style {:resize "none"
               :flex "1"
               :display :flex
               :flex-direction :column
               :width "100%"
               :margin-bottom "1em"}}]
     [:button {:on-click (fn []
                           (copy (->md @text-state))
                           (flash "Copied markdown"))} "Copy md"]][:div {:style {:margin-right "1em" :flex "1" :display :flex :flex-direction :column}}
     [:h2 "HTML"]
     [:textarea
      {:on-change (fn [x]
                    (reset! text-state {:format :html :value  (-> x .-target .-value)}))
                    :value (->html @text-state)
       :style {:resize "none"
               :flex "1"
               :display :flex
               :flex-direction :column
               :width "100%"
               :margin-bottom "1em"}}]
                                                                   [:button {:on-click (fn [] (copy (->html @text-state)) (flash "Copied HTML"))} "Copy html"]]
    [:div {:style {:flex "1" :display :flex :flex-direction :column}}
     [:h2 "Markdown preview"]
     [:div {:style {:flex-grow "1" :margin-bottom "1em"} :dangerouslySetInnerHTML {:__html (->html @text-state)}}]]]])

(defn mount! []
  (reagent/render [app]
                  (.getElementById js/document "app")))

(defn main! []
  (println "Welcome!!")
  (mount!))

(defn reload! []
  (println "Reloaded!!")
  (mount!))

