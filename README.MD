# iDynoMiCS 2.0

Individual-based modelling of microbial communities: reaction–diffusion, growth, and mechanics in 2D/3D.

## Licence

iDynoMiCS 2 is available under the [CeCILL license V2](http://www.cecill.info/index.en.html). The project uses third-party libraries; these are referenced in the Java documentation and summarized in [LICENSE.MD](LICENSE.MD). The CeCILL licence follows the principles of the [GNU GPL](http://www.gnu.org/licenses/gpl-3.0.en.html) and the [BSD licence](https://opensource.org/licenses/BSD-3-Clause).

## Download and use

Pre-built binaries are available in the [releases](https://github.com/kreft/iDynoMiCS-2/releases) section. You need a recent [Java Runtime Environment](https://www.java.com/en/download/manual.jsp) (JRE 11 or later). No extra installation is required. For issues, see the [wiki](https://github.com/kreft/iDynoMiCS-2/wiki).

## Building from source

- **Requirements:** Java 11 (JDK), Maven, and the local dependencies listed in the [wiki](https://github.com/kreft/iDynoMiCS-2/wiki) (JOGL, EXIficient, DasOptimization, etc.).
- **Build:** From the project root, run `mvn clean package`. The runnable JAR is produced in `target/`.
- **Tests:** Run `mvn test` to execute unit and integration tests.

## Getting started

See the iDynoMiCS 2 [wiki](https://github.com/kreft/iDynoMiCS-2/wiki) for tutorials, protocol examples, and usage.

![iDynoMiCS](icons/iDynoMiCS_logo.gif)

## Documentation

| Document | Description |
|----------|-------------|
| [LICENSE.MD](LICENSE.MD) | Licence and third-party components |
| [FORK_NOTES.md](FORK_NOTES.md) | This fork: cleanups and GPU-oriented changes |
| [Docs/README.md](Docs/README.md) | Index of documentation in `Docs/` |
| [Docs/PDEsolverNotes.md](Docs/PDEsolverNotes.md) | PDE solvers: transient vs steady-state |
| [Docs/GPU_ACCELERATION.md](Docs/GPU_ACCELERATION.md) | GPU acceleration for the multigrid solver |
| [Docs/TEST_ASSESSMENT.md](Docs/TEST_ASSESSMENT.md) | Unit and integration test overview |
| [native/README.md](native/README.md) | Building the optional CUDA native library |

## This fork

This repository is a fork that applies code cleanups and adds optional **GPU acceleration** for the reaction–diffusion multigrid solver. Behaviour is unchanged when the GPU library is not used. See [FORK_NOTES.md](FORK_NOTES.md) and [Docs/GPU_ACCELERATION.md](Docs/GPU_ACCELERATION.md) for details.
