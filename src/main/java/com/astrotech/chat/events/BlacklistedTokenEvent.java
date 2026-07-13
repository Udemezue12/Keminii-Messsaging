package com.astrotech.chat.events;



import java.util.Date;






public record BlacklistedTokenEvent(
    String jti,
    Date expiration){

}