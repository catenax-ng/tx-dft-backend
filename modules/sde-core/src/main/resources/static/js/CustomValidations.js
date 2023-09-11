var set_value = true;

function convertToDate(datesrc)
{
	var time_date0=datesrc.substr(datesrc.indexOf(' ')+1);
	var t1=time_date0.split(':');
	var start_date0=datesrc.substr(0,datesrc.indexOf(' ')+1);
	var start = start_date0.split('/');
	var date = new Date(start[2], start[1] - 1, start[0],t1[0],t1[1],t1[2]);
	return date;	
}
function convertToOnlyDate(datesrc)
{
	var start = datesrc.split('/');
	var date = new Date(start[2], start[1] - 1, start[0]);
	return date;	
}

function filenamelength(id,fileName)
{
	if(fileName.length>70)
		{
			$(id).next().html("<br><font class='validation_error'>file name should be less than 70 char.</font>");
			$(id).val("");
			return false;
		}
	else
		{
			$(id).next().html("");
			return true;
		}	
}
function appendDateClearClass(id1)
{
	id=document.getElementById(id1);
	$(id.parentNode).append("<a class=\"clearlink\" href=\"javascript:void()\" id=\""+id1+"_clear\" title=\"Click to clear this field\"></a>");
	$(".clearlink").click(function(){
		$("#"+(this.id).replace("_clear","")).val("");
		});
	}
function replaceAllFirstSpace(str)
		{
			var l=str.length;
			for(var i=0;i<=l;i++)
				{
				if(str.indexOf(' ')==0)
				    str=str.substring(str.indexOf(' ')+1);
				}
			return str;
	}
function replaceAllLastSpace(str)
  {
	for(var i=0;i<=str.length;i++)
		{
		if(str.lastIndexOf(' ')==str.length-1)
		    str=str.substring(0,str.lastIndexOf(' '));
		}
	return str;
  }
function testPattern(value, pattern) 
{   
  // var regExp = new RegExp("^"+pattern+"$","");
   return pattern.test(value);
}
var error_message=[
                   "This Field is required",
                   "Enter Only number",
                   "Invalid Email",
                   "You cannot select more than two days in a week as weekly offs.",
                   "Password and Confirmed Password not match",
                   "Invalid Name",
                   "The password you have entered does not match the guidelines mentioned above. Please re-enter.",
                   "Enter Numbers From 0 to 100",
                   "Enter Correct Mobile No.",
                   "Invalid Number",
                   "Nagraj"
                   ];

var validationclass="validation_error";

function setMessage(field,errorno)
{ 
	    var offset = $(field).offset();
		errorno=parseInt(errorno);
		var left=offset.left+$(field).width()-50;
		var top=offset.top-30;
		if($(field).attr("class").indexOf("daterangepicker")!=-1)
		{
		$(field).next().attr("class",validationclass);
		$(field).next().css({'top':top,'left':left});
		$(field).next().html(error_message[errorno]);
		$(field).next().prepend('<span class="arrow-s" style="left: 4px;top: 19px;"></span>');
		}
	else
		{
		$(field).next().attr("class",validationclass);
		$(field).next().css({'top':top,'left':left});
		$(field).next().html(error_message[errorno]);
		$(field).next().prepend('<span class="arrow-s" style="left: 4px;top: 19px;"></span>');
		}
		setTimeout(function() {removeMessage(field);}, 5000);
}
function removeMessage(field)
{ 
	$(field).next().attr("class","");
	$(field).next().html("");
}
function validateText(src)
{
	$("input,textarea").blur(function(){
		var input_val = $("#"+this.id).val();
		$("#"+this.id).val($.trim(input_val));
		});
	$(".cancel").click(function()
	   {
		 history.back();
 	 	});
	
		$(".required").blur(function(){
			 if ($(this).val()=="")
				 {
				  setMessage(this,0);
				  set_value = true;
				 }
	  	    else if (set_value)
	  	    	removeMessage(this);
	  	 	});
		
	    $(".requiredtb").blur(function(){
		    
		    
		   	if ($(this).val()==""||$(this).val()=="--Select--")
				{
		    		setMessage(this,0);
		    		set_value = true;
		    	}
		    else if (set_value)
		       {
		    	removeMessage(this);
	    	    }
		    });
	    
	   
	    $(".requireddate").blur(function()
	    		{
	    	
				    if ($(this).val()=="")	
				    {	
				    	setMessage(this,0);
				    }
				    else
				    {
				    	removeMessage(this);
				    }
        
	    		}
	    );
	    	
	    $(".requiredWeek").change(function(){
			
	    	var count = $('#holiday_compulsary option:selected').length;
			if ($(this).val()=="" || $(this).val()==null)
				{
				submit='no';
				setMessage(this,0);
				}
			 else if(count>2)
				{
				submit='no';
				setMessage(this,3);
				}
	  	    else
	  	    	{
	  	    	removeMessage(this);
	  	    	}
	    
			});
	
		$(".requireddd").change(function(){
		 if ($(this).val()==""  || $(this).val()=="")
			 {
			     setMessage(this,0);		 
			 }
  	    else
  	    	{
  	    	removeMessage(this);
  	    	}
    
		});
		
		
		$(".passwordrequired").blur(function()
				{
		 
			if ($(this).val()=="")
				{
				setMessage(this,0);
				}
	   	     else
		     {
		    	if($("#password").val()!="" && $("#password2").val()!="")
		    	{
			    	if($("#password").val()==$("#password2").val())
					{
			    		removeMessage($("#password"));
			    		removeMessage($("#password2"));
			    		
					}
			    	else
			    		setMessage(this,4);
			    	}
			    	else
			    		{
			    		removeMessage($("#password"));
			    		removeMessage($("#password2"));
			    		}
		     }    
		});
	
		$(".confirmpassword").blur(function(){
		    
		    if($("#password").val()!="" && $("#password2").val()!="")
		    	{
			    	if($("#password").val()==$("#password2").val())
					{
			    		removeMessage($("#password"));
			    		removeMessage($("#password2"));
					}
			    	else
			    		setMessage(this,4);
			    	}
			    	else
			    		{
			    		removeMessage($("#password"));
			    		removeMessage($("#password2"));
			    		}
		        
		});
	
		
		
		$(".requiredtextformat").blur(function(){
		  
		  if(($(this).val().match(/(.*[0-9].*[0-9].*[0-9])/)))
		    		{
		    		
		    		}
		    	if(($(this).val().match(/(.*[0-9].*[0-9].*[0-9])/)) || ($(this).val().match(/(.*[a-z].*[a-z].*[a-z])/))||($(this).val().match(_))  ){
			    	{
			    	//alert("in if 1");
			    		removeMessage(this);
			    	}	
			    	}
			     if($(this).val().match(/(.*[!,@,#,$,%,^,&,*,?,~])/))
			    	 {
			    	 setMessage(this,5);
			    	 }
		});
		
		
		$(".requiredalphabetics").keyup(function(){
			
			var val=$(this).val();
			var alphaExp = /^[a-zA-Z ]+$/;
			if(val.charAt(0)==' ')
				{  
				   $(this).val("");
				   setMessage(this,5);
				   return false;
				}
			else if($(this).val().match(alphaExp)){
				removeMessage(this);
				}
			else{
				  $(this).val("");
				  setMessage(this,5);
				  return false;
				}
		});
		
		$(".passwordvalidation").blur(function()
				{
			//alert();
		    if ($(this).val()!="")
		    	{
		    	if($(this).val().length>=6){
			    	if($(this).val().match(/(.*[!,@,#,$,%,^,&,*,?,_,~].*[!,@,#,$,%,^,&,*,?,_,~])/)){
			    		if($(this).val().match(/(.*[0-9].*[0-9].*[0-9])/)){
			    			if($("#password").val()!="" && $("#password2").val()!=""){
			    				if($("#password").val()==$("#password2").val())
			    					{
			    					removeMessage($("#password"));
						    		removeMessage($("#password2"));
			    					}
			    				else
			    					setMessage(this,6);
			    				}
			    			else
			    				removeMessage(this);
			    			}	
			    		else
			    			setMessage(this,6);
			    	}
			    	else
			    		setMessage(this,6);
			    	}
			    	else
			    		setMessage(this,6);
	   	     	}    
		});
		
		
		$(".requiredchk").keypress(function(e)
			{
			  if(e.which!=46 && e.which!=8 && e.which!=0 && (e.which<48 || e.which>57&&e.which!=58)&&e.which!=45)
			  {
					if(e.which==44)
						{
						removeMessage(this);
						}
					else
					{
						setMessage(this,1);
				    return false;
				    }
			  }       
			else
				{
				removeMessage(this);
				}
	    
		});
		
		
		$(".requiredchk1").keypress(function(e)
				{
				  if(e.which=46 && e.which!=8 && e.which!=0 && (e.which<48 || e.which>57&&e.which!=58)&&e.which!=45)
				  {
						if(e.which==44)
							{
							removeMessage(this);
							}
						else
						{
							setMessage(this,1);
					    return false;
					    }
				  }       
				else
					{
					removeMessage(this);
					}
		    
			});

		 $(".requiredweight").keyup(function()
		 {
			 
			    var nostr =/^[0-9]*\.?[0-9]*$/;
			    if(testPattern($(this).val(),nostr))
			    		{
			    		removeMessage(this);
			    		}
			    else
			    		{
			    	     setMessage(this,9);
			    		 $(this).val(""); 
			    		 return false;
			    		}
			    
			});
		 
		 $(".requiredweight").keypress(function()
				 {
					 
					    var nostr =/^[0-9]*\.?[0-9]*$/;
					    if(testPattern($(this).val(),nostr))
					    		{
					    		removeMessage(this);
					    		}
					    else
					    		{
					    	     setMessage(this,9);
					    	     $(this).val("");
					    		 return false;
					    		}
					    
					});
	
		$(".requiredchk").blur(function()
				{
			if ($(this).val()==""||$(this).val()=="--Select--")
				removeMessage(this);
		    else
		    	removeMessage(this);
				}
		);
		
		
		 $(".emailaddress").blur(function()
				 {
			    if ($(this).val()=="")
			    	removeMessage(this);
			    else
			    {	
			    	var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
			    	
			    	if(testPattern($(this).val(),emailReg))
			    		{
			    		removeMessage(this);
			    		}
			    	else
			    		{
			    		$(this).val="";
			    		setMessage(this,2);
			    		}
			    }    
			});
		
		$(".requiredemail").blur(function(){
		    if ($(this).val()=="")
		    	{
		    	setMessage(this,0);
		    	}
	   	     else
		    {
		    	var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
		    	if(testPattern($(this).val(),emailReg))
		    		{
		    		removeMessage(this);
		    		}
		    	else
		    		{
		    		setMessage(this,2);
		    		}
		    }    
		});
		
		
		
		
		$(".requireddigit").keypress(function(e)
				{
			if(e.which!=46 && e.which!=8 && e.which!=0 && (e.which<48 || e.which>57))
			  {
				setMessage(this,1);
			    return false;
			  }       
			else{
				removeMessage(this);
			}
	    
		});

	    
		$(".requireddigit").blur(function(e){
			  if(e.which!=46 && e.which!=8 && e.which!=0 && (e.which<48 || e.which>57))
			  {
				  setMessage(this,1);
			    return false;
			  } 
			  else 		    
			  if ($(this).val()==""||$(this).val()=="--Select--")
			  {
				  setMessage(this,0);
			  }
			else
				{
				removeMessage(this);
				}
	    
		});
		
		$(".requiredpercentage").keypress(function(e)
				{
			if(e.which!=46 && e.which!=8 && e.which!=0 && (e.which<48 || e.which>57))
			  {
				setMessage(this,4);
			    return false;
			  }else if ($(this).val()>100 || $(this).val()<0)
			  {
				  setMessage(this,7);
			  }
			else{
				removeMessage(this);
			}
	    
		});
		$(".requiredpercentage").blur(function(e)
				{
			if(e.which!=46 && e.which!=8 && e.which!=0 && (e.which<48 || e.which>57))
			  {
				setMessage(this,4);
			    return false;
			  }else if ($(this).val()>100 || $(this).val()<0)
			  {
				  setMessage(this,7);
			  }
			else{
				removeMessage(this);
			}
	    
		});
		$(".mobile").blur(function()
				{
			 alert("mobile");
			if ($(this).val()<999999999)
				 setMessage(this,8);
		    else
		    	removeMessage(this);
				}
		);
		
	$(".validateform").click(
			function ()
			{
				var submit='yes';
				$(".validation_error").each(function()
				{
					var v_error=$(this).attr("class");
					if(v_error=="validation_error")
							submit='no';
					else
						removeMessage(this);
				}
				);
			    $(".requiredtb").each(function()
			    		{
						    if ($(this).val()=="")	
						    {	
						    	submit='no';
						    	setMessage(this,0);
						    	set_value = true;
						    }
						    else if (set_value)
						    {
						    	removeMessage(this);
						    }
		        
			    		}
			    );
			    $(".requireddate").each(function()
			    		{
						    if ($(this).val()=="")	
						    {	
						    	submit='no';
						    	setMessage(this,0);
						    }
						    else
						    {
						    	removeMessage(this);
						    }
		        
			    		}
			    );
			   
			    $(".subButton").each(function()
			    		{
					//alert("in sub....");
						 submit='no';
				        $(this).next().html("<br><font class='validation_error'>User Name already exists</font> ");
				
			  	 	});
			    
			    $(".required").each(function(){
					 if ($(this).val()==""  || $(this).val()=="")
						 {
						 submit='no';
						 setMessage(this,0);
						 }
			  	    else 
			  	    	removeMessage(this);
			  	 	});
			    
			    $(".mobile").each(function()
						{
					
					if ($(this).val()<999999999)
						{
						submit='no';
						 setMessage(this,8);}
				    else
				    	removeMessage(this);
						}
				);
			    
			    $(".passwordrequired").each(function(){
				   //alert("in required password..");
			    	if ($(this).val()=="")
				    	{
				    	submit='no';
				    	setMessage(this,0);
				    	}
				    else
				     {	
				    	if($("#password").val()!="" && $("#password2").val()!="")
				    	{
				    	if($("#password").val()==$("#password2").val())
						{
				    		removeMessage($("#password"));
				    		removeMessage($("#password2"));
						}
				    	else
				    		{
				    		submit='no';
				    		setMessage(this,6);
				    		}
				    		}
				    	else
				    		{
				    		removeMessage($("#password"));
				    		removeMessage($("#password2"));
				    		}
				    }    
				});
			    
			   
			    $(".confirmpassword").each(function(){
					   //alert("in required password..");
				    	
					   
					    	if($("#password").val()!="" && $("#password2").val()!="")
					    	{
					    	if($("#password").val()==$("#password2").val())
							{
					    		removeMessage($("#password"));
					    		removeMessage($("#password2"));
							}
					    	else
					    		{
					    		submit='no';
					    		setMessage(this,6);
					    		}
					    		}
					    	else
					    		{
					    		removeMessage($("#password"));
					    		removeMessage($("#password2"));
					    		}
					    
					});
			    
			      $(".passwordvalidation").each(function()
			    		  {
				
			    	if ($(this).val()!="")
				    	{
				    	if($(this).val().length>=6){
					    	if($(this).val().match(/(.*[!,@,#,$,%,^,&,*,?,_,~].*[!,@,#,$,%,^,&,*,?,_,~])/)){
					    		if($(this).val().match(/(.*[0-9].*[0-9].*[0-9])/)){
					    			if($("#password").val()!="" && $("#password2").val()!="")
					    			  {
					    				if($("#password").val()==$("#password2").val())
					    					{
					    					removeMessage($("#password"));
								    		removeMessage($("#password2"));
					    					}
					    				else
					    					{
					    					submit='no';
					    					setMessage(this,6);
					    				}
					    			}
					    			else
					    				removeMessage(this);
					    			}	
					    		else
					    			{
					    			submit='no';
					    			setMessage(this,6);
					    	    }
					    	}
					    	else
					    		{
					    		submit='no';
					    		setMessage(this,6);
					    	}
					    	}
					    	else
					    		{
					    		submit='no';
					    		setMessage(this,6);
					    		}
				    	}  
				    else if($("#password").val()!="" && $("#password2").val()=="")
    				{
				    	submit='no';
				    	setMessage(this,0);
    				}
				    else if($("#password").val()=="" && $("#password2").val()!="")
    				{
				    	submit='no';
				    	setMessage(this,0);
    				}
				});
			    
			    
			   
			    
			    $(".requiredtextformat").each(function()
			    		{
					//alert("in dept validation. each..");
				
			    	if(e.which!=46 && e.which!=8 && e.which!=0 && (e.which<48 || e.which>57&&e.which!=58)&&e.which!=45)
					  {
							if(e.which==44)
								{
								}
							else
							{
						    return false;
						    }
					  }       
			    	
			    	if ($(this).val()!="")
			    	{	//alert("in dept validation...");	    	
				    	
						
						if(($(this).val().match(/(.*[0-9].*[0-9].*[0-9])/)) || ($(this).val().match(/(.*[a-z].*[a-z].*[a-z])/))||($(this).val().match(_))  ){
				    	{
				    		removeMessage(this);
				    	}	
				    	}
				    	
				     if($(this).val().match(/(.*[!,@,#,$,%,^,&,*,?,~])/))
				    	 setMessage(this,7);
				    				    	
		   	     	}    
				});
			    $(".emailaddress").each(function(){
				    if ($(this).val()=="")
				    	removeMessage(this);
				    else
				    {	
				    	var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
				    	//alert($(this).val());
				    	if(testPattern($(this).val(),emailReg))
				    		removeMessage(this);
				    	
				    	else
				    		{
				    		$(this).val="";
				    		setMessage(this,2);
				    		 submit='no';
				    		}
				    }    
				});
			    $(".requiredemail").each(function(){
				    if ($(this).val()=="")
				    	{
				    	//alert("in mail...");
				    	 setMessage(this,0);
				    	  submit='no';
				    	}
				    else
				    {	
				    	var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
				    	if(testPattern($(this).val(),emailReg))
				    		$(this).next().html("");
				    	else
				    		{
				    		setMessage(this,2);
				    		submit='no';
				    		}
				    }    
				});
			    $(".requiredweight").each(function()
				   		 {
			    	        
				   			   var nostr =/^[0-9]*\.?[0-9]*$/;
				   			    if($(this).val().length>1)
				   			    {
				   			    	if(testPattern($(this).val(),nostr))
				   			    	{
				   			    		removeMessage(this);
				   			    	}
				   			    	else
				   			    	{
				   			    		setMessage(this,9);
				   			    		submit='no';
				   			    		
				   			    	}
				   			    }
				   			});
			    
			   
			    $(".validate_no").each(function()
						{
					//alert("in each .........");
			    	 if($(this).val().match(/(.*[0-9].*[0-9].*[0-9])/))
			    		 removeMessage(this);
			    	else
			    		{
			    		 submit='no';
			    		 setMessage(this,3);
			    		}
			   });
				$(".requireddd").each(function(){
				if ($(this).val()==""  || $(this).val()==null || $(this).val()=="")
					{
					submit='no';
					setMessage(this,0);
					}
		  	    else
		  	    	{
		  	    	removeMessage(this);
		  	    	}
		    
				});
				$(".requiredWeek").each(function(){
					 var count = $('#holiday_compulsary option:selected').length;
					if ($(this).val()=="" ||  $(this).val()==null)
						{
						submit='no';
						setMessage(this,0);

						}
					 else if(count>2)
						{
						submit='no';
						setMessage(this,4);
						}
			  	    else
			  	    	{
			  	    	removeMessage(this);
			  	    	}
			    
					});
			
			    $(".requireddigit").each(function()
			    		{
						    if ($(this).val()=="")	
						    {	
						    	submit='no';
						    	setMessage(this,0);
						    }
						    else
						    	{
						    	removeMessage(this);
						    	}
		        
			    		}
			    );
			    
			    $(".requiredBudget").keypress(function(e){
					//alert(e.which);
					if( e.which!=8 && e.which!=0  && (e.which<48 || e.which>57))
					  {
						   if(e.which!=44)
						   {
							   setMessage(this,3);
							    return false;
						   }
					  }       
					else
						if( e.which==9)
						  {
							if($(this).val().length<=4)
								removeMessage(this);
							else
								{
								setMessage(this,4);
								return false;
								}
						  }  
					else{
						
						if($(this).val().length<=4)
							removeMessage(this);
						else{
							setMessage(this,4);
							return false;
							}
						}
				});
				$(".requiredBudget").keyup(function(e){
				
					if( e.which==8 || e.which=='\t')
					  {
						if($(this).val().length<=4)
							removeMessage(this);
						else
							setMessage(this,4);
				
					  }       
				});
				$(".requiredBudget").keydown(function(e){
					if( e.which==9)
					  {
						if($(this).val().length<=4)
							removeMessage(this);
						else
							setMessage(this,4);
					  }       
				});
				// alert(submit)
				if(submit=='yes')
					{
					 eval('document.'+src+'.submit()');
					 $("#container").hide();
					 $("#contentdiv").hide();
					 $("#page_loader").show();
					}
				else
					return false;
 	 });
}

