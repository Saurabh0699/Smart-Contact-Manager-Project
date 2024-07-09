console.log("this is script file")
const toggleSidebar=()=>{

   if($(".sidebar").is(":visible")) {
   //true
   //band karana hai

   $(".sidebar").css("display", "none");
   $(".content").css("margin-left", "0%");

   } else {
   //false
   //show karana hai

   $(".sidebar").css("display", "block");
   $(".content").css("margin-left", "20%");

   }

};
