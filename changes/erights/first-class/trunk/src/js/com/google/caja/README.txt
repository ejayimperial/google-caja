Caja modules that can run correctly whether translated or untranslated
should be named "<ident>-caja.js". But if they use only the Cajita
subset of Caja, they should be named "<ident>-cajita.js" instead. In
both cases, <ident> should be the main name the module exports.

caja.js             The Caja runtime library
                    caja.js exports the globals "caja" and "___".

triv-logger.js      Optional: Registers log functions with caja.js to
log-to-console.js   receive diagnostic reports. If you use the
                    squarefree shell, you can instead simply do
		    "___.setLogFunc(print)".

generics-cajita.js  Optional: Virtually adds consensus extensions
extensions.js       (like the static generic Array.slice()) to various
                    primordial objects.

permissive.js       Diagnostic only: Installs a keeper that
                    effectively allows all legacy APIs, but logs the
                    first time they're faulted in so we know what to
                    tame.

JSON.js             A Caja-friendly JSON library.
                    Will probably be replaced with a JSON-cajita.js

Q-caja.js           Asynchronous promise-based distributed capability
                    messaging with JSON over https using
                    web-keys. This will likely be extended for
                    asynchronous local inter-vat messaging as well.

Brand-cajita.js     Makes sealer/unsealer pairs for rights amplification

Mint-cajita.js      A simple money example

