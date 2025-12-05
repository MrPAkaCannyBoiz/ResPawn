using ApiContracts;
using ApiContracts.Dtos;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceExceptions;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace WebAPI.Controllers
{
    [Route("reseller/login")]
    [ApiController]
    public class ResellerLoginController : ControllerBase
    {
        private readonly IResellerLoginService resellerLoginService;

        public ResellerLoginController(IResellerLoginService resellerLoginService)
        {
            this.resellerLoginService =resellerLoginService;
        }
            [HttpPost]
    [ProducesResponseType(typeof(ResellerLoginResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> LoginResellerAsync([FromBody] ResellerLoginDto dto, CancellationToken ct)
        {
            if (!ModelState.IsValid) return ValidationProblem(ModelState);
        var grpcReq = new Com.Respawnmarket.ResellerLoginRequest
        {
            Username = dto.Username,
            Password = dto.Password
        };
            try
            {
                 var grpcRes = await resellerLoginService.LoginResellerAsync(grpcReq, ct);
            var api = new ResellerLoginResponseDto
            {
                Id = grpcRes.Id,
                Username = grpcRes.Username,
              
            };
            return Ok(api);
        }
        catch (InvalidLoginException ex)
        {
            return NotFound(new ProblemDetails
            {
                Title = "Reseller not found",
                Detail = ex.Message,
                Status = StatusCodes.Status404NotFound
            });
        }
        catch (ApplicationException ex)
        {
            return Problem(detail: ex.Message, statusCode: StatusCodes.Status500InternalServerError);
        }
    }
    }
}